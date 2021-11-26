package org.entur.lamassu.listener.listeners;

import org.entur.lamassu.listener.CacheEntryListenerDelegate;

import javax.cache.event.CacheEntryCreatedListener;
import javax.cache.event.CacheEntryEvent;
import javax.cache.event.CacheEntryExpiredListener;
import javax.cache.event.CacheEntryListenerException;
import javax.cache.event.CacheEntryRemovedListener;
import javax.cache.event.CacheEntryUpdatedListener;
import java.io.Serializable;

public class CacheEntryListener<T> implements
        CacheEntryCreatedListener<String, T>,
        CacheEntryUpdatedListener<String, T>,
        CacheEntryRemovedListener<String, T>,
        CacheEntryExpiredListener<String, T>,
        Serializable {

    private final transient CacheEntryListenerDelegate<T> delegate;

    public CacheEntryListener(CacheEntryListenerDelegate<T> delegate) {
        this.delegate = delegate;
    }

    @Override
    public void onCreated(Iterable<CacheEntryEvent<? extends String, ? extends T>> iterable) throws CacheEntryListenerException {
        if (iterable != null) {
            delegate.onCreated(iterable);
        }
    }

    @Override
    public void onRemoved(Iterable<CacheEntryEvent<? extends String, ? extends T>> iterable) throws CacheEntryListenerException {
        if (iterable != null) {
            delegate.onRemoved(iterable);
        }
    }

    @Override
    public void onUpdated(Iterable<CacheEntryEvent<? extends String, ? extends T>> iterable) throws CacheEntryListenerException {
        if (iterable != null) {
            delegate.onUpdated(iterable);
        }
    }

    @Override
    public void onExpired(Iterable<CacheEntryEvent<? extends String, ? extends T>> iterable) throws CacheEntryListenerException {
        if (iterable != null) {
            delegate.onExpired(iterable);
        }
    }
}
