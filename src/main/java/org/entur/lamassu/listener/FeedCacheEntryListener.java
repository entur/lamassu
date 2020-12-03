package org.entur.lamassu.listener;

import org.entur.lamassu.cache.VehicleCache;
import org.entur.lamassu.model.gbfs.v2_1.FreeBikeStatus;
import org.entur.lamassu.model.gbfs.v2_1.GBFSBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.cache.Cache;
import javax.cache.event.CacheEntryCreatedListener;
import javax.cache.event.CacheEntryEvent;
import javax.cache.event.CacheEntryListener;
import javax.cache.event.CacheEntryListenerException;
import javax.cache.event.CacheEntryUpdatedListener;
import java.io.Serializable;

public class FeedCacheEntryListener implements
        CacheEntryCreatedListener<String, GBFSBase>,
        CacheEntryUpdatedListener<String, GBFSBase>,
        Serializable, CacheEntryListener<String, GBFSBase> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private VehicleCache vehicleCache;

    public FeedCacheEntryListener(VehicleCache vehicleCache) {
        this.vehicleCache = vehicleCache;
    }

    @Override
    public void onCreated(Iterable<CacheEntryEvent<? extends String, ? extends GBFSBase>> iterable) throws CacheEntryListenerException {
        updateVehicles(iterable);
    }

    @Override
    public void onUpdated(Iterable<CacheEntryEvent<? extends String, ? extends GBFSBase>> iterable) throws CacheEntryListenerException {
        updateVehicles(iterable);
    }

    private void updateVehicles(Iterable<CacheEntryEvent<? extends String, ? extends GBFSBase>> iterable) {
        if (iterable != null) {
            for (var entry : iterable) {
                if (entry.getValue() instanceof FreeBikeStatus) {
                    var freeBikeStatus = (FreeBikeStatus) entry.getValue();
                    vehicleCache.updateAll(freeBikeStatus.getData().getBikes());
                    logger.debug("Added vehicles to vehicle cache from feed {}", entry.getKey());
                }
            }
        }
    }
}
