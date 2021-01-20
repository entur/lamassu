package org.entur.lamassu.listener.listeners;

import org.entur.lamassu.listener.FeedCacheEntryListenerDelegate;
import org.entur.lamassu.listener.FeedCacheListener;
import org.entur.lamassu.model.gbfs.v2_1.GBFSBase;
import org.entur.lamassu.model.gbfs.v2_1.VehicleTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.cache.Cache;
import javax.cache.configuration.FactoryBuilder;
import javax.cache.configuration.MutableCacheEntryListenerConfiguration;

@Component
public class VehicleTypesCacheListener extends AbstractCacheListener<VehicleTypes> implements FeedCacheListener<VehicleTypes> {

    private MutableCacheEntryListenerConfiguration<String, GBFSBase> listenerConfiguration;

    @Autowired
    public VehicleTypesCacheListener(Cache<String, GBFSBase> cache, FeedCacheEntryListenerDelegate<VehicleTypes> delegate) {
        super(cache, delegate);
    }

    protected MutableCacheEntryListenerConfiguration<String, GBFSBase> getListenerConfiguration(FeedCacheEntryListenerDelegate<? extends GBFSBase> delegate) {
        if (listenerConfiguration == null) {
            listenerConfiguration = new MutableCacheEntryListenerConfiguration<>(
                    FactoryBuilder.factoryOf(
                            new FeedCacheEntryListener(delegate)
                    ),
                    FactoryBuilder.factoryOf(
                            VehicleTypesEventFilter.class
                    ),
                    false,
                    false
            );
        }
        return listenerConfiguration;
    }
}
