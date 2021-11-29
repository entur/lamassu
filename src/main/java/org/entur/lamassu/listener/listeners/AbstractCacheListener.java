package org.entur.lamassu.listener.listeners;

import org.entur.lamassu.listener.CacheEntryListenerDelegate;

import javax.cache.Cache;
import javax.cache.configuration.MutableCacheEntryListenerConfiguration;

public abstract class AbstractCacheListener<T> {

    private final Cache<String, T> cache;
    private final CacheEntryListenerDelegate<T> delegate;

    protected AbstractCacheListener(Cache<String, T> cache, CacheEntryListenerDelegate<T> delegate) {
        this.cache = cache;
        this.delegate = delegate;
    }

    public void startListening() {
        cache.registerCacheEntryListener(getListenerConfiguration(delegate));
    }

    public void stopListening() {
        cache.deregisterCacheEntryListener(getListenerConfiguration(delegate));
    }

    protected abstract MutableCacheEntryListenerConfiguration<String, T> getListenerConfiguration(CacheEntryListenerDelegate<T> delegate);
}
