package org.entur.lamassu.listener.listeners;

import org.entur.gbfs.v2_2.free_bike_status.GBFSFreeBikeStatus;

import javax.cache.event.CacheEntryEvent;
import javax.cache.event.CacheEntryEventFilter;
import javax.cache.event.CacheEntryListenerException;

public class FreeBikeStatusEventFilter implements CacheEntryEventFilter<String, Object> {
    @Override
    public boolean evaluate(CacheEntryEvent<? extends String, ?> cacheEntryEvent) throws CacheEntryListenerException {
        return cacheEntryEvent.getValue() instanceof GBFSFreeBikeStatus;
    }
}
