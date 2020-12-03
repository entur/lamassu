package org.entur.lamassu.listener;

import org.entur.lamassu.model.gbfs.v2_1.FreeBikeStatus;
import org.redisson.api.GeoEntry;
import org.redisson.api.RGeo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.cache.event.CacheEntryCreatedListener;
import javax.cache.event.CacheEntryEvent;
import javax.cache.event.CacheEntryExpiredListener;
import javax.cache.event.CacheEntryListener;
import javax.cache.event.CacheEntryListenerException;
import javax.cache.event.CacheEntryRemovedListener;
import javax.cache.event.CacheEntryUpdatedListener;
import java.io.Serializable;

public class VehicleCacheEntryListener implements
        CacheEntryCreatedListener<String, FreeBikeStatus.Bike>,
        CacheEntryUpdatedListener<String, FreeBikeStatus.Bike>,
        CacheEntryExpiredListener<String, FreeBikeStatus.Bike>,
        CacheEntryRemovedListener<String, FreeBikeStatus.Bike>,
        Serializable, CacheEntryListener<String, FreeBikeStatus.Bike> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    RGeo<String> spatialIndex;

    public VehicleCacheEntryListener(RGeo<String> spatialIndex) {
        this.spatialIndex = spatialIndex;
    }

    @Override
    public void onCreated(Iterable<CacheEntryEvent<? extends String, ? extends FreeBikeStatus.Bike>> iterable) throws CacheEntryListenerException {
        updateSpatialIndex(iterable);
    }

    @Override
    public void onUpdated(Iterable<CacheEntryEvent<? extends String, ? extends FreeBikeStatus.Bike>> iterable) throws CacheEntryListenerException {
        updateSpatialIndex(iterable);
    }

    private void updateSpatialIndex(Iterable<CacheEntryEvent<? extends String, ? extends FreeBikeStatus.Bike>> iterable) {
        if (iterable != null) {
            for (var entry : iterable) {
                var vehicle= (FreeBikeStatus.Bike) entry.getValue();
                spatialIndex.add(vehicle.getLon(), vehicle.getLat(), vehicle.getBikeId());
                logger.debug("Added/updated vehicle to spatial index: {}", vehicle.getBikeId());
            }
        }
    }

    @Override
    public void onExpired(Iterable<CacheEntryEvent<? extends String, ? extends FreeBikeStatus.Bike>> iterable) throws CacheEntryListenerException {
        removeFromSpatialIndex(iterable);
    }

    @Override
    public void onRemoved(Iterable<CacheEntryEvent<? extends String, ? extends FreeBikeStatus.Bike>> iterable) throws CacheEntryListenerException {
        removeFromSpatialIndex(iterable);
    }

    private void removeFromSpatialIndex(Iterable<CacheEntryEvent<? extends String, ? extends FreeBikeStatus.Bike>> iterable) {
        if (iterable != null) {
            for (var entry : iterable) {
                var vehicle= (FreeBikeStatus.Bike) entry.getValue();
                spatialIndex.remove(vehicle.getBikeId());
                logger.debug("Removed vehicle from spatial index: {}", vehicle.getBikeId());
            }
        }
    }
}
