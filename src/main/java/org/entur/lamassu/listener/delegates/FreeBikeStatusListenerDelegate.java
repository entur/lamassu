package org.entur.lamassu.listener.delegates;

import org.entur.lamassu.cache.PricingPlanCache;
import org.entur.lamassu.cache.SystemCache;
import org.entur.lamassu.cache.VehicleCache;
import org.entur.lamassu.cache.VehicleSpatialIndex;
import org.entur.lamassu.cache.VehicleTypeCache;
import org.entur.lamassu.config.feedprovider.FeedProviderConfig;
import org.entur.lamassu.listener.CacheEntryListenerDelegate;
import org.entur.lamassu.mapper.VehicleMapper;
import org.entur.lamassu.model.entities.Vehicle;
import org.entur.lamassu.model.feedprovider.FeedProvider;
import org.entur.lamassu.model.gbfs.v2_1.FreeBikeStatus;
import org.entur.lamassu.model.gbfs.v2_1.GBFSBase;
import org.entur.lamassu.util.SpatialIndexIdUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.cache.event.CacheEntryEvent;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class FreeBikeStatusListenerDelegate implements CacheEntryListenerDelegate<GBFSBase, FreeBikeStatus> {

    private final VehicleMapper vehicleMapper;
    private final VehicleCache vehicleCache;
    private final VehicleTypeCache vehicleTypeCache;
    private final PricingPlanCache pricingPlanCache;
    private final SystemCache systemCache;
    private final FeedProviderConfig feedProviderConfig;
    private final VehicleSpatialIndex spatialIndex;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public FreeBikeStatusListenerDelegate(
            VehicleMapper vehicleMapper,
            VehicleCache vehicleCache,
            VehicleTypeCache vehicleTypeCache,
            PricingPlanCache pricingPlanCache,
            SystemCache systemCache,
            FeedProviderConfig feedProviderConfig,
            VehicleSpatialIndex spatialIndex
    ) {
        this.vehicleMapper = vehicleMapper;
        this.vehicleCache = vehicleCache;
        this.vehicleTypeCache = vehicleTypeCache;
        this.pricingPlanCache = pricingPlanCache;
        this.systemCache = systemCache;
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
        // noop
    }

    @Override
    public void onExpired(CacheEntryEvent<? extends String, GBFSBase> event) {
        // noop
    }

    private void addOrUpdateVehicles(CacheEntryEvent<? extends String, ? extends GBFSBase> event) {
        var split = event.getKey().split("_");
        var feedProvider = feedProviderConfig.get(split[split.length - 1]);
        var freeBikeStatusFeed = (FreeBikeStatus) event.getValue();

        var originalVehicleIds = freeBikeStatusFeed.getData().getBikes().stream()
                .map(FreeBikeStatus.Bike::getBikeId).collect(Collectors.toSet());
        var vehicleTypeIds = freeBikeStatusFeed.getData().getBikes().stream()
                .map(FreeBikeStatus.Bike::getVehicleTypeId).collect(Collectors.toSet());
        var pricingPlanIds = freeBikeStatusFeed.getData().getBikes().stream()
                .map(FreeBikeStatus.Bike::getPricingPlanId).collect(Collectors.toSet());

        try {
            var originalVehicles = vehicleCache.getAllAsMap(originalVehicleIds);
            var vehicleTypes = vehicleTypeCache.getAllAsMap(vehicleTypeIds);
            var pricingPlans = pricingPlanCache.getAllAsMap(pricingPlanIds);
            var system = systemCache.get(feedProvider.getName());

            var vehicles = freeBikeStatusFeed.getData().getBikes().stream()
                    .map(vehicle -> vehicleMapper.mapVehicle(
                            vehicle,
                            vehicleTypes.get(vehicle.getVehicleTypeId()),
                            pricingPlans.get(vehicle.getPricingPlanId()),
                            system
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
                    .collect(Collectors.toMap(v -> getVehicleCacheKey(v, feedProvider), v -> v));

            Map<String, Vehicle> spatialIndexUpdateMap = new java.util.HashMap<>(Map.of());

            vehicles.forEach(vehicle -> {
                var spatialIndexId = SpatialIndexIdUtil.createSpatialIndexId(vehicle, feedProvider);
                var previousVehicle = originalVehicles.get(vehicle.getId());
                if (previousVehicle != null) {
                    var oldSpatialIndexId = SpatialIndexIdUtil.createSpatialIndexId(previousVehicle, feedProvider);
                    if (!oldSpatialIndexId.equalsIgnoreCase(spatialIndexId)) {
                        spatialIndex.remove(spatialIndexId);
                    }
                }
                spatialIndexUpdateMap.put(spatialIndexId, vehicle);
            });

            vehicleCache.updateAll(vehiclesMap);
            spatialIndex.addAll(spatialIndexUpdateMap);

            logger.info("Added vehicles to vehicle cache from feed {}", event.getKey());
        } catch (NullPointerException e) {
            logger.warn("Caught NullPointerException when updating vehicle cache from freeBikeStatusFeed: {}", freeBikeStatusFeed, e);
        }
    }

    private String getVehicleCacheKey(Vehicle vehicle, FeedProvider feedProvider) {
        return vehicle.getId() + "_" + feedProvider.getName();
    }
}
