package org.entur.lamassu.cache.impl;

import org.entur.lamassu.cache.GBFSFeedCache;
import org.entur.lamassu.cache.PricingPlanCache;
import org.entur.lamassu.cache.VehicleCache;
import org.entur.lamassu.cache.VehicleTypeCache;
import org.entur.lamassu.mapper.PricingPlanMapper;
import org.entur.lamassu.mapper.VehicleMapper;
import org.entur.lamassu.mapper.VehicleTypeMapper;
import org.entur.lamassu.model.FeedProvider;
import org.entur.lamassu.model.gbfs.v2_1.GBFSBase;
import org.entur.lamassu.model.gbfs.v2_1.GBFSFeedName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.cache.Cache;
import javax.cache.configuration.FactoryBuilder;
import javax.cache.configuration.MutableCacheEntryListenerConfiguration;

@Component
public class GBFSFeedCacheImpl implements GBFSFeedCache {

    @Autowired
    private Cache<String, GBFSBase> cache;

    @Autowired
    private VehicleCache vehicleCache;

    @Autowired
    private VehicleMapper vehicleMapper;

    @Autowired
    private VehicleTypeCache vehicleTypeCache;

    @Autowired
    private VehicleTypeMapper vehicleTypeMapper;

    @Autowired
    private PricingPlanCache pricingPlanCache;

    @Autowired
    private PricingPlanMapper pricingPlanMapper;

    private MutableCacheEntryListenerConfiguration<String, GBFSBase> feedCacheListenerConfiguration;

    @Override
    public GBFSBase find(GBFSFeedName feedName, FeedProvider feedProvider) {
        return cache.get(getKey(feedName, feedProvider.getName()));
    }

    @Override
    public void update(GBFSFeedName feedName, FeedProvider feedProvider, GBFSBase feed) {
        String key = getKey(
                feedName,
                feedProvider.getName()
        );
        cache.put(key, feed);
    }

    @Override
    public void startListening() {
        cache.registerCacheEntryListener(getListenerConfiguration());
    }

    @Override
    public void stopListening() {
        cache.deregisterCacheEntryListener(getListenerConfiguration());
    }

    private String getKey(GBFSFeedName feedName, String providerName) {
        return mergeStrings(feedName.toValue(), providerName);
    }

    private String mergeStrings(String first, String second) {
        return String.format("%s_%s", first, second);
    }

    private MutableCacheEntryListenerConfiguration<String, GBFSBase> getListenerConfiguration() {
        if (feedCacheListenerConfiguration == null) {
            feedCacheListenerConfiguration = new MutableCacheEntryListenerConfiguration<>(
                    FactoryBuilder.factoryOf(
                            new FeedCacheEntryListener(vehicleCache, vehicleMapper, vehicleTypeCache, vehicleTypeMapper, pricingPlanCache, pricingPlanMapper)
                    ),
                    FactoryBuilder.factoryOf(
                            FeedCacheEventFilter.class
                    ),
                    false,
                    false
            );
        }
        return feedCacheListenerConfiguration;
    }
}
