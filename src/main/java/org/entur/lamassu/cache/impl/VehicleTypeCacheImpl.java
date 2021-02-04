package org.entur.lamassu.cache.impl;

import org.entur.lamassu.cache.VehicleTypeCache;
import org.entur.lamassu.model.entities.VehicleType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.cache.Cache;

@Component
public class VehicleTypeCacheImpl extends EntityCacheImpl<VehicleType> implements VehicleTypeCache {
    protected VehicleTypeCacheImpl(@Autowired Cache<String, VehicleType> cache) {
        super(cache);
    }
}
