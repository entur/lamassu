package org.entur.lamassu.listener.delegates;

import org.entur.lamassu.cache.PricingPlanCache;
import org.entur.lamassu.cache.VehicleCache;
import org.entur.lamassu.cache.VehicleSpatialIndex;
import org.entur.lamassu.cache.VehicleTypeCache;
import org.entur.lamassu.config.feedprovider.FeedProviderConfig;
import org.entur.lamassu.listener.CacheEntryListenerDelegate;
import org.entur.lamassu.mapper.VehicleMapper;
import org.entur.lamassu.model.entities.Vehicle;
import org.entur.lamassu.model.gbfs.v2_1.FreeBikeStatus;
import org.entur.lamassu.model.gbfs.v2_1.GBFSBase;
import org.entur.lamassu.util.SpatialIndexIdUtil;
import org.redisson.client.RedisException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.cache.event.CacheEntryEvent;
import java.util.stream.Collectors;

@Component
public class FreeBikeStatusListenerDelegate implements CacheEntryListenerDelegate<GBFSBase, FreeBikeStatus> {

    private final VehicleMapper vehicleMapper;
    private final VehicleCache vehicleCache;
    private final VehicleTypeCache vehicleTypeCache;
    private final PricingPlanCache pricingPlanCache;
    private final FeedProviderConfig feedProviderConfig;
    private final VehicleSpatialIndex spatialIndex;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public FreeBikeStatusListenerDelegate(
            VehicleMapper vehicleMapper,
            VehicleCache vehicleCache,
            VehicleTypeCache vehicleTypeCache,
            PricingPlanCache pricingPlanCache,
            FeedProviderConfig feedProviderConfig,
            VehicleSpatialIndex spatialIndex
    ) {
        this.vehicleMapper = vehicleMapper;
        this.vehicleCache = vehicleCache;
        this.vehicleTypeCache = vehicleTypeCache;
        this.pricingPlanCache = pricingPlanCache;
        this.feedProviderConfig = feedProviderConfig;
        this.spatialIndex = spatialIndex;
    }

    @Override
    public void onCreated(CacheEntryEvent<? extends String, GBFSBase> event) {
        addOrUpdateVehicles(event);
    }

    @Override
    public void onUpdated(CacheEntryEvent<? extends String, GBFSBase> event) {
        addOrUpdateVehicles(event);
    }

    @Override
    public void onRemoved(CacheEntryEvent<? extends String, GBFSBase> event) {
        // TODO implement
    }

    @Override
    public void onExpired(CacheEntryEvent<? extends String, GBFSBase> event) {
        // TODO implement
    }

    private void addOrUpdateVehicles(CacheEntryEvent<? extends String, ? extends GBFSBase> event) {
        var split = event.getKey().split("_");
        var feedProvider = feedProviderConfig.get(split[split.length - 1]);
        var freeBikeStatusFeed = (FreeBikeStatus) event.getValue();
        try {
            var vehicles = freeBikeStatusFeed.getData().getBikes().stream()
                    .map(vehicle -> vehicleMapper.mapVehicle(
                            vehicle,
                            vehicleTypeCache.get(vehicle.getVehicleTypeId()),
                            pricingPlanCache.get(vehicle.getPricingPlanId())
                    )).collect(Collectors.toList());

            if (vehicles.stream().distinct().count() != (long) vehicles.size()) {
                logger.warn("Found duplicates in freeBikeStatusFeed with key={}", event.getKey());
            }

            vehicles = vehicles.stream()
                    .distinct()
                    .filter(vehicle -> vehicle.getVehicleType() != null)
                    .filter(vehicle -> vehicle.getPricingPlan() != null)
                    .collect(Collectors.toList());

            var vehiclesMap = vehicles.stream()
                    .collect(Collectors.toMap(Vehicle::getId, v -> v));

            vehicles.forEach(vehicle -> {
                    var spatialIndexId = SpatialIndexIdUtil.createSpatialIndexId(vehicle, feedProvider);
                    var previousVehicle = vehicleCache.get(vehicle.getId());
                    if (previousVehicle != null) {
                        var oldSpatialIndexId = SpatialIndexIdUtil.createSpatialIndexId(previousVehicle, feedProvider);
                        if (!oldSpatialIndexId.equalsIgnoreCase(spatialIndexId)) {
                            removeFromSpatialIndex(oldSpatialIndexId, previousVehicle);
                        }
                    }
                    addOrUpdateSpatialIndex(spatialIndexId, vehicle);
                });

            vehicleCache.updateAll(vehiclesMap);
            logger.info("Added vehicles to vehicle cache from feed {}", event.getKey());
        } catch (NullPointerException e) {
            logger.warn("Caught NullPointerException when updating vehicle cache from freeBikeStatusFeed: {}", freeBikeStatusFeed, e);
        }
    }

    private void addOrUpdateSpatialIndex(String id, Vehicle vehicle) {
        try {
            long added = spatialIndex.add(vehicle.getLon(), vehicle.getLat(), id);
            if (added > 0) {
                logger.debug("Added vehicle to spatial index: indexId={} vehicle={}", id, vehicle);
            } else {
                logger.debug("Updated vehicle in spatial index: indexId={} vehicle={}", id, vehicle);
            }
        } catch (RedisException | IllegalArgumentException e) {
            logger.warn("Caught exception when trying to add vehicle to spatial index for vehicle={}", vehicle, e);
        }
    }

    private void removeFromSpatialIndex(String id, Vehicle vehicle) {
        spatialIndex.remove(id);
        logger.debug("Removed vehicle from spatial index: {}", vehicle.getId());
    }
}
