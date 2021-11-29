package org.entur.lamassu.listener;

import javax.cache.event.CacheEntryEvent;

public interface CacheEntryListenerDelegate<T> {
    void onCreated(Iterable<CacheEntryEvent<? extends String, ? extends T>> iterable);
    void onUpdated(Iterable<CacheEntryEvent<? extends String, ? extends T>> iterable);
    void onRemoved(Iterable<CacheEntryEvent<? extends String, ? extends T>> iterable);
    void onExpired(Iterable<CacheEntryEvent<? extends String, ? extends T>> iterable);
}

