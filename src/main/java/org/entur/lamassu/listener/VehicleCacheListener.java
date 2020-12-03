package org.entur.lamassu.listener;

import org.entur.lamassu.model.gbfs.v2_1.FreeBikeStatus;
import org.redisson.api.RGeo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.cache.Cache;
import javax.cache.configuration.FactoryBuilder;
import javax.cache.configuration.MutableCacheEntryListenerConfiguration;

@Component
public class VehicleCacheListener {

    @Autowired
    Cache<String, FreeBikeStatus.Bike> vehicleCache;

    @Autowired
    RGeo<String> spatialIndex;

    MutableCacheEntryListenerConfiguration<String, FreeBikeStatus.Bike> vehicleCacheListenerConfiguration;

    @PostConstruct
    public void init() {
        vehicleCacheListenerConfiguration = new MutableCacheEntryListenerConfiguration<>(
                FactoryBuilder.factoryOf(
                        new VehicleCacheEntryListener(spatialIndex)
                ),
                null,
                false,
                false
        );
    }

    public void startListening() {
        vehicleCache.registerCacheEntryListener(vehicleCacheListenerConfiguration);
    }

    public void stopListening() {
        vehicleCache.deregisterCacheEntryListener(vehicleCacheListenerConfiguration);
    }
}
