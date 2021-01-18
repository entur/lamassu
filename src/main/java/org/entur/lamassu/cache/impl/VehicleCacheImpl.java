package org.entur.lamassu.cache.impl;

import org.entur.lamassu.cache.VehicleCache;
import org.entur.lamassu.cache.VehicleSpatialIndex;
import org.entur.lamassu.model.Vehicle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.cache.Cache;
import javax.cache.configuration.FactoryBuilder;
import javax.cache.configuration.MutableCacheEntryListenerConfiguration;

@Component
public class VehicleCacheImpl extends EntityCacheImpl<Vehicle> implements VehicleCache {

    @Autowired
    private VehicleSpatialIndex spatialIndex;

    private MutableCacheEntryListenerConfiguration<String, Vehicle> vehicleCacheListenerConfiguration;

    protected VehicleCacheImpl(@Autowired Cache<String, Vehicle> cache) {
        super(cache);
    }

    @Override
    public void startListening() {
        cache.registerCacheEntryListener(getListenerConfiguration());
    }

    @Override
    public void stopListening() {
        cache.deregisterCacheEntryListener(getListenerConfiguration());
    }

    private MutableCacheEntryListenerConfiguration<String, Vehicle> getListenerConfiguration() {
        if (vehicleCacheListenerConfiguration == null) {
            vehicleCacheListenerConfiguration = new MutableCacheEntryListenerConfiguration<>(
                    FactoryBuilder.factoryOf(
                            new VehicleCacheEntryListener(spatialIndex)
                    ),
                    null,
                    false,
                    false
            );
        }
        return vehicleCacheListenerConfiguration;
    }
}
