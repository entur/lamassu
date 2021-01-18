package org.entur.lamassu.cache.impl;

import org.entur.lamassu.cache.VehicleSpatialIndex;
import org.entur.lamassu.model.Vehicle;
import org.entur.lamassu.model.gbfs.v2_1.FreeBikeStatus;
import org.redisson.api.RGeo;
import org.redisson.client.RedisException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.cache.event.CacheEntryCreatedListener;
import javax.cache.event.CacheEntryEvent;
import javax.cache.event.CacheEntryExpiredListener;
import javax.cache.event.CacheEntryListener;
import javax.cache.event.CacheEntryRemovedListener;
import javax.cache.event.CacheEntryUpdatedListener;
import java.io.Serializable;

public class VehicleCacheEntryListener implements
        CacheEntryCreatedListener<String, Vehicle>,
        CacheEntryUpdatedListener<String, Vehicle>,
        CacheEntryExpiredListener<String, Vehicle>,
        CacheEntryRemovedListener<String, Vehicle>,
        Serializable, CacheEntryListener<String, Vehicle> {

    private final transient Logger logger = LoggerFactory.getLogger(this.getClass());

    private final transient VehicleSpatialIndex spatialIndex;

    public VehicleCacheEntryListener(VehicleSpatialIndex spatialIndex) {
        this.spatialIndex = spatialIndex;
    }

    @Override
    public void onCreated(Iterable<CacheEntryEvent<? extends String, ? extends Vehicle>> iterable) {
        updateSpatialIndex(iterable);
    }

    @Override
    public void onUpdated(Iterable<CacheEntryEvent<? extends String, ? extends Vehicle>> iterable) {
        updateSpatialIndex(iterable);
    }

    @Override
    public void onExpired(Iterable<CacheEntryEvent<? extends String, ? extends Vehicle>> iterable) {
        removeFromSpatialIndex(iterable);
    }

    @Override
    public void onRemoved(Iterable<CacheEntryEvent<? extends String, ? extends Vehicle>> iterable) {
        removeFromSpatialIndex(iterable);
    }

    private void updateSpatialIndex(Iterable<CacheEntryEvent<? extends String, ? extends Vehicle>> iterable) {
        if (iterable != null) {
            for (var entry : iterable) {
                var vehicle= entry.getValue();
                try {
                    long added = spatialIndex.add(vehicle.getLon(), vehicle.getLat(), vehicle.getId());
                    if (added > 0) {
                        logger.debug("Added vehicle to spatial index: {}", vehicle.getId());
                    } else {
                        logger.debug("Updated vehicle in spatial index: {}", vehicle.getId());
                    }
                } catch (RedisException e) {
                    logger.warn("Caught exception when trying to add vehicle to spatial index for vehicle={}", vehicle, e);
                }
            }
        }
    }

    private void removeFromSpatialIndex(Iterable<CacheEntryEvent<? extends String, ? extends Vehicle>> iterable) {
        if (iterable != null) {
            for (var entry : iterable) {
                var vehicle= entry.getValue();
                spatialIndex.remove(vehicle.getId());
                logger.debug("Removed vehicle from spatial index: {}", vehicle.getId());
            }
        }
    }
}
