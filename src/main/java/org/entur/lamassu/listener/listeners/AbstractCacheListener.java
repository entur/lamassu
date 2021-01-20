package org.entur.lamassu.listener.listeners;

import org.entur.lamassu.listener.CacheEntryListenerDelegate;

import javax.cache.Cache;
import javax.cache.configuration.MutableCacheEntryListenerConfiguration;

public abstract class AbstractCacheListener<T, S> {

    private final Cache<String, T> cache;
    private final CacheEntryListenerDelegate<T, S> delegate;

    protected AbstractCacheListener(Cache<String, T> cache, CacheEntryListenerDelegate<T, S> delegate) {
        this.cache = cache;
        this.delegate = delegate;
    }

    public void startListening() {
        cache.registerCacheEntryListener(getListenerConfiguration(delegate));
    }

    public void stopListening() {
        cache.deregisterCacheEntryListener(getListenerConfiguration(delegate));
    }

    protected abstract MutableCacheEntryListenerConfiguration<String, T> getListenerConfiguration(CacheEntryListenerDelegate<T, S> delegate);
}
