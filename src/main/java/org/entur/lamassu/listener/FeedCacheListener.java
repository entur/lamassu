package org.entur.lamassu.listener;

import org.entur.lamassu.cache.VehicleCache;
import org.entur.lamassu.model.gbfs.v2_1.GBFSBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.cache.Cache;
import javax.cache.configuration.FactoryBuilder;
import javax.cache.configuration.MutableCacheEntryListenerConfiguration;

@Component
public class FeedCacheListener {

    @Autowired
    Cache<String, GBFSBase> feedCache;

    @Autowired
    VehicleCache vehicleCache;

    MutableCacheEntryListenerConfiguration<String, GBFSBase> feedCacheListenerConfiguration;

    @PostConstruct
    public void init() {
        feedCacheListenerConfiguration = new MutableCacheEntryListenerConfiguration<>(
                FactoryBuilder.factoryOf(
                        new FeedCacheEntryListener(vehicleCache)
                ),
                FactoryBuilder.factoryOf(
                        FeedCacheEventFilter.class
                ),
                false,
                false
        );
    }

    public void startListening() {
        feedCache.registerCacheEntryListener(feedCacheListenerConfiguration);
    }

    public void stopListening() {
        feedCache.deregisterCacheEntryListener(feedCacheListenerConfiguration);
    }
}
