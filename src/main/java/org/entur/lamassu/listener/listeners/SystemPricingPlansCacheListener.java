package org.entur.lamassu.listener.listeners;

import org.entur.lamassu.listener.FeedCacheEntryListenerDelegate;
import org.entur.lamassu.listener.FeedCacheListener;
import org.entur.lamassu.model.gbfs.v2_1.GBFSBase;
import org.entur.lamassu.model.gbfs.v2_1.SystemPricingPlans;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.cache.Cache;
import javax.cache.configuration.FactoryBuilder;
import javax.cache.configuration.MutableCacheEntryListenerConfiguration;

@Component
public class SystemPricingPlansCacheListener implements FeedCacheListener<SystemPricingPlans> {
    private final Cache<String, GBFSBase> cache;
    private final FeedCacheEntryListenerDelegate<SystemPricingPlans> delegate;
    private MutableCacheEntryListenerConfiguration<String, GBFSBase> listenerConfiguration;

    @Autowired
    public SystemPricingPlansCacheListener(Cache<String, GBFSBase> cache, FeedCacheEntryListenerDelegate<SystemPricingPlans> delegate) {
        this.cache = cache;
        this.delegate = delegate;
    }

    @Override
    public void startListening() {
        cache.registerCacheEntryListener(getListenerConfiguration());
    }

    @Override
    public void stopListening() {
        cache.registerCacheEntryListener(getListenerConfiguration());
    }

    private MutableCacheEntryListenerConfiguration<String, GBFSBase> getListenerConfiguration() {
        if (listenerConfiguration == null) {
            listenerConfiguration = new MutableCacheEntryListenerConfiguration<>(
                    FactoryBuilder.factoryOf(
                            new FeedCacheEntryListener(delegate)
                    ),
                    FactoryBuilder.factoryOf(
                            SystemPricingPlansEventFilter.class
                    ),
                    false,
                    false
            );
        }
        return listenerConfiguration;
    }
}
