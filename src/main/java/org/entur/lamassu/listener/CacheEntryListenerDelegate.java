package org.entur.lamassu.listener;

import javax.cache.event.CacheEntryEvent;

public interface CacheEntryListenerDelegate<T, S> {
    void onCreated(CacheEntryEvent<? extends String, T> event);
    void onUpdated(CacheEntryEvent<? extends String, T> event);
    void onRemoved(CacheEntryEvent<? extends String, T> event);
    void onExpired(CacheEntryEvent<? extends String, T> event);
}

