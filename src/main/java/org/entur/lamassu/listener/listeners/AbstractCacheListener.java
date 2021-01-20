package org.entur.lamassu.listener.listeners;

import org.entur.lamassu.listener.FeedCacheEntryListenerDelegate;
import org.entur.lamassu.listener.FeedCacheListener;
import org.entur.lamassu.model.gbfs.v2_1.GBFSBase;

import javax.cache.Cache;
import javax.cache.configuration.MutableCacheEntryListenerConfiguration;

public abstract class AbstractCacheListener<T extends GBFSBase> implements FeedCacheListener<T> {

    private final Cache<String, GBFSBase> cache;
    private final FeedCacheEntryListenerDelegate<? extends GBFSBase> delegate;

    protected AbstractCacheListener(Cache<String, GBFSBase> cache, FeedCacheEntryListenerDelegate<? extends GBFSBase> delegate) {
        this.cache = cache;
        this.delegate = delegate;
    }

    @Override
    public void startListening() {
        cache.registerCacheEntryListener(getListenerConfiguration(delegate));
    }
    @Override
    public void stopListening() {
        cache.deregisterCacheEntryListener(getListenerConfiguration(delegate));
    }

    protected abstract MutableCacheEntryListenerConfiguration<String, GBFSBase> getListenerConfiguration(FeedCacheEntryListenerDelegate<? extends GBFSBase> delegate);
}
