package org.entur.lamassu.service.impl;

import org.entur.lamassu.cache.VehicleCache;
import org.entur.lamassu.cache.VehicleSpatialIndex;
import org.entur.lamassu.cache.SpatialIndexId;
import org.entur.lamassu.model.entities.Vehicle;
import org.entur.lamassu.service.VehicleFilterParameters;
import org.entur.lamassu.service.VehicleQueryParameters;
import org.entur.lamassu.service.VehiclesNearbyService;
import org.entur.lamassu.util.VehicleFilter;
import org.redisson.api.GeoOrder;
import org.redisson.api.GeoUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class VehiclesNearbyServiceImpl implements VehiclesNearbyService {

    private final VehicleSpatialIndex spatialIndex;
    private final VehicleCache vehicleCache;

    @Autowired
    public VehiclesNearbyServiceImpl(VehicleSpatialIndex spatialIndex, VehicleCache vehicleCache) {
        this.spatialIndex = spatialIndex;
        this.vehicleCache = vehicleCache;
    }

    @Override
    public List<Vehicle> getVehiclesNearby(VehicleQueryParameters vehicleQueryParameters, VehicleFilterParameters vehicleFilterParameters) {
        Double longitude = vehicleQueryParameters.getLon();
        Double latitude = vehicleQueryParameters.getLat();
        Double range = vehicleQueryParameters.getRange();
        Integer count = vehicleQueryParameters.getCount();

        List<String> indexIds = spatialIndex.radius(longitude, latitude, range, GeoUnit.METERS, GeoOrder.ASC);


        var stream = indexIds.stream()
                .map(SpatialIndexId::fromString)
                .filter(Objects::nonNull)
                .filter(id -> VehicleFilter.filterVehicle(id, vehicleFilterParameters));

        if (count != null) {
            stream = stream.limit(count.longValue());
        }

        Set<String> vehicleIds = stream.map(this::getVehicleCacheKey)
                .collect(Collectors.toSet());

        return vehicleCache.getAll(vehicleIds);
    }

    private String getVehicleCacheKey(SpatialIndexId spatialIndexId) {
        return spatialIndexId.getVehicleId() + "_" + spatialIndexId.getOperator();
    }

}
