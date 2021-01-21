package org.entur.lamassu.cache;

import org.redisson.api.GeoOrder;
import org.redisson.api.GeoUnit;

import java.util.List;

public interface VehicleSpatialIndex {
    long add(double longitude, double latitude, String id);
    boolean remove(String id);
    List<String> radius(double longitude, double latitude, double radius, GeoUnit geoUnit, GeoOrder geoOrder, int count);
}
