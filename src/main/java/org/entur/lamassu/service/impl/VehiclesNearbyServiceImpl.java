package org.entur.lamassu.service.impl;

import org.entur.lamassu.cache.VehicleCache;
import org.entur.lamassu.cache.VehicleSpatialIndex;
import org.entur.lamassu.cache.SpatialIndexId;
import org.entur.lamassu.model.Vehicle;
import org.entur.lamassu.service.VehicleFilterParameters;
import org.entur.lamassu.service.VehicleQueryParameters;
import org.entur.lamassu.service.VehiclesNearbyService;
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

    @Autowired
    VehicleSpatialIndex spatialIndex;

    @Autowired
    VehicleCache vehicleCache;

    @Override
    public List<Vehicle> getVehiclesNearby(VehicleQueryParameters vehicleQueryParameters, VehicleFilterParameters vehicleFilterParameters) {
        Double longitude = vehicleQueryParameters.getLon();
        Double latitude = vehicleQueryParameters.getLat();
        Double range = vehicleQueryParameters.getRange();
        Integer count = vehicleQueryParameters.getCount();

        List<String> indexIds = spatialIndex.radius(longitude, latitude, range, GeoUnit.METERS, GeoOrder.ASC);

        Set<String> vehicleIds = indexIds.stream()
                .map(SpatialIndexId::fromString)
                .filter(Objects::nonNull)
                .filter(id -> filterVehicle(id, vehicleFilterParameters))
                .limit(count)
                .map(id -> id.getOperator() + "_" + id.getVehicleId())
                .collect(Collectors.toSet());

        return vehicleCache.getAll(vehicleIds);
    }

    private boolean filterVehicle(SpatialIndexId parsedId, VehicleFilterParameters filters) {
        if (filters.getOperators() != null && !filters.getOperators().contains(parsedId.getOperator())) {
            return false;
        }

        if (filters.getCodespaces() != null && !filters.getCodespaces().contains(parsedId.getCodespace())) {
            return false;
        }

        if (filters.getFormFactors() != null && !filters.getFormFactors().contains(parsedId.getFormFactor())) {
            return false;
        }

        if (filters.getPropulsionTypes() != null && !filters.getPropulsionTypes().contains(parsedId.getPropulsionTypes())) {
            return false;
        }

        if (Boolean.FALSE.equals(filters.getIncludeReserved()) && Boolean.TRUE.equals(parsedId.getReserved())) {
            return false;
        }

        if (Boolean.FALSE.equals(filters.getIncludeDisabled()) && Boolean.TRUE.equals(parsedId.getDisabled())) {
            return false;
        }

        return true;
    }
}
