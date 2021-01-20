package org.entur.lamassu.listener;

import org.entur.lamassu.model.gbfs.v2_1.GBFSBase;

import javax.cache.event.CacheEntryEvent;

public interface FeedCacheEntryListenerDelegate<T extends GBFSBase> {
    void onCreated(CacheEntryEvent<? extends String, ? extends GBFSBase> event);
    void onUpdated(CacheEntryEvent<? extends String, ? extends GBFSBase> event);
    void onRemoved(CacheEntryEvent<? extends String, ? extends GBFSBase> event);
}
