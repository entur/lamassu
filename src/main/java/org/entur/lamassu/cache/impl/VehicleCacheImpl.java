package org.entur.lamassu.cache.impl;

import org.entur.lamassu.cache.VehicleCache;
import org.entur.lamassu.model.entities.Vehicle;
import org.redisson.api.RMapCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class VehicleCacheImpl extends EntityCacheImpl<Vehicle> implements VehicleCache {
    protected VehicleCacheImpl(@Autowired RMapCache<String, Vehicle> cache) {
        super(cache);
    }
}

