package org.entur.lamassu.cache;

import org.entur.lamassu.model.entities.Vehicle;
import org.redisson.api.GeoOrder;
import org.redisson.api.GeoUnit;

import java.util.List;
import java.util.Map;

public interface VehicleSpatialIndex {
    void addAll(Map<String, Vehicle> spatialIndexUpdateMap);
    void remove(String id);
    List<String> radius(Double longitude, Double latitude, Double radius, GeoUnit geoUnit, GeoOrder geoOrder);
}
