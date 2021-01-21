package org.entur.lamassu.cache.impl;

import org.entur.lamassu.cache.VehicleSpatialIndex;
import org.redisson.api.GeoOrder;
import org.redisson.api.GeoUnit;
import org.redisson.api.RGeo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class VehicleSpatialIndexImpl implements VehicleSpatialIndex {
    @Autowired
    RGeo<String> spatialIndex;

    @Override
    public long add(double longitude, double latitude, String id) {
        return spatialIndex.add(longitude, latitude, id);
    }

    @Override
    public boolean remove(String id) {
        return spatialIndex.remove(id);
    }

    @Override
    public List<String> radius(double longitude, double latitude, double radius, GeoUnit geoUnit, GeoOrder geoOrder, int count) {
        return spatialIndex.radius(longitude, latitude, radius, geoUnit, geoOrder, count);
    }
}
