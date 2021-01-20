package org.entur.lamassu.listener.listeners;

import org.entur.lamassu.listener.CacheListener;
import org.entur.lamassu.listener.CacheEntryListenerDelegate;
import org.entur.lamassu.model.gbfs.v2_1.FreeBikeStatus;
import org.entur.lamassu.model.gbfs.v2_1.GBFSBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.cache.Cache;
import javax.cache.configuration.FactoryBuilder;
import javax.cache.configuration.MutableCacheEntryListenerConfiguration;

@Component
public class FreeBikeStatusCacheListener extends AbstractCacheListener<GBFSBase, FreeBikeStatus> implements CacheListener<FreeBikeStatus> {
    private MutableCacheEntryListenerConfiguration<String, GBFSBase> listenerConfiguration;

    @Autowired
    public FreeBikeStatusCacheListener(Cache<String, GBFSBase> cache, CacheEntryListenerDelegate<GBFSBase, FreeBikeStatus> delegate) {
        super(cache, delegate);
    }

    @Override
    protected MutableCacheEntryListenerConfiguration<String, GBFSBase> getListenerConfiguration(CacheEntryListenerDelegate<GBFSBase, FreeBikeStatus> delegate) {
        if (listenerConfiguration == null) {
            listenerConfiguration = new MutableCacheEntryListenerConfiguration<>(
                    FactoryBuilder.factoryOf(
                            new CacheEntryListener<>(delegate)
                    ),
                    FactoryBuilder.factoryOf(
                            FreeBikeStatusEventFilter.class
                    ),
                    false,
                    false
            );
        }
        return listenerConfiguration;
    }
}
