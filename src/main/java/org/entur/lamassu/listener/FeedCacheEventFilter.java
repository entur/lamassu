package org.entur.lamassu.listener;

import org.entur.lamassu.model.gbfs.v2_1.FreeBikeStatus;
import org.entur.lamassu.model.gbfs.v2_1.GBFSBase;

import javax.cache.event.CacheEntryEvent;
import javax.cache.event.CacheEntryEventFilter;
import javax.cache.event.CacheEntryListenerException;

public class FeedCacheEventFilter implements CacheEntryEventFilter<String, GBFSBase> {
    @Override
    public boolean evaluate(CacheEntryEvent cacheEntryEvent) throws CacheEntryListenerException {
        var feed = (GBFSBase) cacheEntryEvent.getValue();
        return feed instanceof FreeBikeStatus;
    }
}
