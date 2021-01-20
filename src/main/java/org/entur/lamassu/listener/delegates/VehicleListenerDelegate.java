package org.entur.lamassu.listener.delegates;

import org.entur.lamassu.cache.VehicleSpatialIndex;
import org.entur.lamassu.listener.CacheEntryListenerDelegate;
import org.entur.lamassu.model.Vehicle;
import org.redisson.client.RedisException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.cache.event.CacheEntryEvent;

@Component
public class VehicleListenerDelegate implements CacheEntryListenerDelegate<Vehicle, Vehicle> {
    private final VehicleSpatialIndex spatialIndex;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public VehicleListenerDelegate(VehicleSpatialIndex spatialIndex) {
        this.spatialIndex = spatialIndex;
    }

    @Override
    public void onCreated(CacheEntryEvent<? extends String, Vehicle> event) {
        updateSpatialIndex(event);
    }

    @Override
    public void onUpdated(CacheEntryEvent<? extends String, Vehicle> event) {
        updateSpatialIndex(event);
    }

    @Override
    public void onRemoved(CacheEntryEvent<? extends String, Vehicle> event) {
        removeFromSpatialIndex(event);
    }

    @Override
    public void onExpired(CacheEntryEvent<? extends String, Vehicle> event) {
        removeFromSpatialIndex(event);
    }

    private void updateSpatialIndex(CacheEntryEvent<? extends String, ? extends Vehicle> event) {
        var vehicle= event.getValue();
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

    private void removeFromSpatialIndex(CacheEntryEvent<? extends String, ? extends Vehicle> event) {
        var vehicle= event.getValue();
        spatialIndex.remove(vehicle.getId());
        logger.debug("Removed vehicle from spatial index: {}", vehicle.getId());
    }
}
