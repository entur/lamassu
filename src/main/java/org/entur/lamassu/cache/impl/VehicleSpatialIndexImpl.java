package org.entur.lamassu.cache.impl;

import io.lettuce.core.RedisException;
import org.entur.lamassu.cache.VehicleSpatialIndex;
import org.entur.lamassu.model.entities.Vehicle;
import org.redisson.api.GeoEntry;
import org.redisson.api.GeoOrder;
import org.redisson.api.GeoUnit;
import org.redisson.api.RGeo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class VehicleSpatialIndexImpl implements VehicleSpatialIndex {
    private final RGeo<String> spatialIndex;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public VehicleSpatialIndexImpl(RGeo<String> spatialIndex) {
        this.spatialIndex = spatialIndex;
    }

    @Override
    public void addAll(Map<String, Vehicle> spatialIndexUpdateMap) {
        try {
            spatialIndex.addAsync(spatialIndexUpdateMap.entrySet().stream()
                    .map(this::map).toArray(GeoEntry[]::new));
        } catch (RedisException e) {
            logger.warn("Caught exception while adding entries to spatialIndex", e);
        }
    }

    private GeoEntry map(Map.Entry<String, Vehicle> entry) {
        var key = entry.getKey();
        var vehicle = entry.getValue();
        return new GeoEntry(vehicle.getLon(), vehicle.getLat(), key);
    }

    @Override
    public void remove(String id) {
        spatialIndex.removeAsync(id);
    }

    @Override
    public void removeAll(Set<String> ids) {
        spatialIndex.removeAllAsync(ids);
    }

    @Override
    public List<String> radius(Double longitude, Double latitude, Double radius, GeoUnit geoUnit, GeoOrder geoOrder) {
        return spatialIndex.radius(longitude, latitude, radius, geoUnit, geoOrder);
    }
}
