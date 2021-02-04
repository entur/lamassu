package org.entur.lamassu.cache.impl;

import org.entur.lamassu.cache.VehicleCache;
import org.entur.lamassu.model.entities.Vehicle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.cache.Cache;

@Component
public class VehicleCacheImpl extends EntityCacheImpl<Vehicle> implements VehicleCache {
    protected VehicleCacheImpl(@Autowired Cache<String, Vehicle> cache) {
        super(cache);
    }
}

