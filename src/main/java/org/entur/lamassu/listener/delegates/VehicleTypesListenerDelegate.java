package org.entur.lamassu.listener.delegates;

import org.entur.lamassu.cache.VehicleTypeCache;
import org.entur.lamassu.listener.FeedCacheEntryListenerDelegate;
import org.entur.lamassu.mapper.VehicleTypeMapper;
import org.entur.lamassu.model.gbfs.v2_1.GBFSBase;
import org.entur.lamassu.model.gbfs.v2_1.VehicleTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.cache.event.CacheEntryEvent;
import java.util.stream.Collectors;

@Component
public class VehicleTypesListenerDelegate implements FeedCacheEntryListenerDelegate<VehicleTypes> {

    private final VehicleTypeMapper vehicleTypeMapper;
    private final VehicleTypeCache vehicleTypeCache;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public VehicleTypesListenerDelegate(VehicleTypeMapper vehicleTypeMapper, VehicleTypeCache vehicleTypeCache) {
        this.vehicleTypeMapper = vehicleTypeMapper;
        this.vehicleTypeCache = vehicleTypeCache;
    }

    @Override
    public void onCreated(CacheEntryEvent<? extends String, ? extends GBFSBase> event) {
        addOrUpdateVehicleType(event);
    }

    @Override
    public void onUpdated(CacheEntryEvent<? extends String, ? extends GBFSBase> event) {
        addOrUpdateVehicleType(event);
    }

    @Override
    public void onRemoved(CacheEntryEvent<? extends String, ? extends GBFSBase> event) {
        // TODO implement
    }

    public void addOrUpdateVehicleType(CacheEntryEvent<? extends String, ? extends GBFSBase> event) {
        var vehicleTypesFeed = (VehicleTypes) event.getValue();
        try {
            var vehicleTypes = vehicleTypesFeed.getData().getVehicleTypes().stream()
                    .map(vehicleTypeMapper::mapVehicleType).collect(Collectors.toList());
            vehicleTypeCache.updateAll(vehicleTypes);
            logger.info("Added vehicle types to vehicle types cache from feed {}", event.getKey());
        } catch (NullPointerException e) {
            logger.warn("Caught NullPointerException when updating vehicle type cache from vehicleTypesFeed: {}", vehicleTypesFeed, e);
        }
    }
}
