package org.entur.lamassu.listener.listeners;

import org.entur.lamassu.listener.CacheEntryListenerDelegate;

import javax.cache.event.CacheEntryCreatedListener;
import javax.cache.event.CacheEntryEvent;
import javax.cache.event.CacheEntryExpiredListener;
import javax.cache.event.CacheEntryListenerException;
import javax.cache.event.CacheEntryRemovedListener;
import javax.cache.event.CacheEntryUpdatedListener;
import java.io.Serializable;

public class CacheEntryListener<T, S> implements
        CacheEntryCreatedListener<String, T>,
        CacheEntryUpdatedListener<String, T>,
        CacheEntryRemovedListener<String, T>,
        CacheEntryExpiredListener<String, T>,
        Serializable {

    private final transient CacheEntryListenerDelegate<T, S> delegate;

    public CacheEntryListener(CacheEntryListenerDelegate<T, S> delegate) {
        this.delegate = delegate;
    }

    @Override
    public void onCreated(Iterable<CacheEntryEvent<? extends String, ? extends T>> iterable) throws CacheEntryListenerException {
        if (iterable != null) {
            for (CacheEntryEvent<? extends String, ? extends T> entry : iterable) {
                delegate.onCreated((CacheEntryEvent<? extends String, T>) entry);
            }
        }
    }

    @Override
    public void onRemoved(Iterable<CacheEntryEvent<? extends String, ? extends T>> iterable) throws CacheEntryListenerException {
        if (iterable != null) {
            for (CacheEntryEvent<? extends String, ? extends T> entry : iterable) {
                delegate.onRemoved((CacheEntryEvent<? extends String, T>) entry);
            }
        }
    }

    @Override
    public void onUpdated(Iterable<CacheEntryEvent<? extends String, ? extends T>> iterable) throws CacheEntryListenerException {
        if (iterable != null) {
            for (CacheEntryEvent<? extends String, ? extends T> entry : iterable) {
                delegate.onUpdated((CacheEntryEvent<? extends String, T>) entry);
            }
        }
    }

    @Override
    public void onExpired(Iterable<CacheEntryEvent<? extends String, ? extends T>> iterable) throws CacheEntryListenerException {
        if (iterable != null) {
            for (CacheEntryEvent<? extends String, ? extends T> entry : iterable) {
                delegate.onExpired((CacheEntryEvent<? extends String, T>) entry);
            }
        }
    }
}
