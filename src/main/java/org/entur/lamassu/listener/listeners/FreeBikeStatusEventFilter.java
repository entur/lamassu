package org.entur.lamassu.listener.listeners;

import org.entur.lamassu.model.gbfs.v2_1.FreeBikeStatus;
import org.entur.lamassu.model.gbfs.v2_1.GBFSBase;

import javax.cache.event.CacheEntryEvent;
import javax.cache.event.CacheEntryEventFilter;
import javax.cache.event.CacheEntryListenerException;

public class FreeBikeStatusEventFilter implements CacheEntryEventFilter<String, GBFSBase> {
    @Override
    public boolean evaluate(CacheEntryEvent<? extends String, ? extends GBFSBase> cacheEntryEvent) throws CacheEntryListenerException {
        return cacheEntryEvent.getValue() instanceof FreeBikeStatus;
    }
}
