package org.entur.lamassu.listener.listeners;

import org.entur.gbfs.v2_2.free_bike_status.GBFSFreeBikeStatus;
import org.entur.lamassu.listener.CacheListener;
import org.entur.lamassu.listener.CacheEntryListenerDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.cache.Cache;
import javax.cache.configuration.FactoryBuilder;
import javax.cache.configuration.MutableCacheEntryListenerConfiguration;

@Component
public class FreeBikeStatusCacheListener extends AbstractCacheListener<Object, GBFSFreeBikeStatus> implements CacheListener<GBFSFreeBikeStatus> {
    private MutableCacheEntryListenerConfiguration<String, Object> listenerConfiguration;

    @Autowired
    public FreeBikeStatusCacheListener(Cache<String, Object> cache, CacheEntryListenerDelegate<Object, GBFSFreeBikeStatus> delegate) {
        super(cache, delegate);
    }

    @Override
    protected MutableCacheEntryListenerConfiguration<String, Object> getListenerConfiguration(CacheEntryListenerDelegate<Object, GBFSFreeBikeStatus> delegate) {
        if (listenerConfiguration == null) {
            listenerConfiguration = new MutableCacheEntryListenerConfiguration<>(
                    FactoryBuilder.factoryOf(
                            new CacheEntryListener<>(delegate)
                    ),
                    FactoryBuilder.factoryOf(
                            FreeBikeStatusEventFilter.class
                    ),
                    true,
                    false
            );
        }
        return listenerConfiguration;
    }
}
