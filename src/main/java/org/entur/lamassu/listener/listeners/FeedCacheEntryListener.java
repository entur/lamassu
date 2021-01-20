package org.entur.lamassu.listener.listeners;

import org.entur.lamassu.listener.FeedCacheEntryListenerDelegate;
import org.entur.lamassu.model.gbfs.v2_1.GBFSBase;

import javax.cache.event.CacheEntryCreatedListener;
import javax.cache.event.CacheEntryEvent;
import javax.cache.event.CacheEntryListener;
import javax.cache.event.CacheEntryListenerException;
import javax.cache.event.CacheEntryRemovedListener;
import javax.cache.event.CacheEntryUpdatedListener;
import java.io.Serializable;

public class FeedCacheEntryListener implements
        CacheEntryCreatedListener<String, GBFSBase>,
        CacheEntryUpdatedListener<String, GBFSBase>,
        CacheEntryRemovedListener<String, GBFSBase>,
        Serializable, CacheEntryListener<String, GBFSBase> {

    private final transient FeedCacheEntryListenerDelegate<? extends GBFSBase> delegate;

    public FeedCacheEntryListener(FeedCacheEntryListenerDelegate<? extends GBFSBase> delegate) {
        this.delegate = delegate;
    }

    @Override
    public void onCreated(Iterable<CacheEntryEvent<? extends String, ? extends GBFSBase>> iterable) throws CacheEntryListenerException {
        if (iterable != null) {
            for (var entry : iterable) {
                delegate.onCreated(entry);
            }
        }
    }

    @Override
    public void onRemoved(Iterable<CacheEntryEvent<? extends String, ? extends GBFSBase>> iterable) throws CacheEntryListenerException {
        if (iterable != null) {
            for (var entry : iterable) {
                delegate.onRemoved(entry);
            }
        }
    }

    @Override
    public void onUpdated(Iterable<CacheEntryEvent<? extends String, ? extends GBFSBase>> iterable) throws CacheEntryListenerException {
        if (iterable != null) {
            for (var entry : iterable) {
                delegate.onUpdated(entry);
            }
        }
    }
}
