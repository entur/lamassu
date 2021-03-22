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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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

        var vehicleIds = freeBikeStatusFeed.getData().getBikes().stream()
                .map(FreeBikeStatus.Bike::getBikeId).collect(Collectors.toSet());

        Set<String> vehicleIdsToRemove = Set.of();

        if (event.isOldValueAvailable()) {
            var oldFreeBikeStatusFeed = (FreeBikeStatus) event.getOldValue();
            vehicleIdsToRemove = oldFreeBikeStatusFeed.getData().getBikes().stream()
                    .map(FreeBikeStatus.Bike::getBikeId).collect(Collectors.toSet());
            vehicleIdsToRemove.removeAll(vehicleIds);

            logger.debug("Found {} vehicleIds to remove from old free_bike_status feed", vehicleIdsToRemove.size());

            vehicleIds.addAll(vehicleIdsToRemove);
        } else {
            logger.debug("Old free_bike_status feed was not available. Unable to find vehicles to remove from old feed.");
        }

        var vehicleTypeIds = freeBikeStatusFeed.getData().getBikes().stream()
                .map(FreeBikeStatus.Bike::getVehicleTypeId).collect(Collectors.toSet());
        var pricingPlanIds = freeBikeStatusFeed.getData().getBikes().stream()
                .map(FreeBikeStatus.Bike::getPricingPlanId).collect(Collectors.toSet());

        var originalVehicles = vehicleCache.getAllAsMap(vehicleIds);
        var vehicleTypes = vehicleTypeCache.getAllAsMap(vehicleTypeIds);
        var pricingPlans = pricingPlanCache.getAllAsMap(pricingPlanIds);
        var system = systemCache.get(feedProvider.getName());

        var vehicles = freeBikeStatusFeed.getData().getBikes().stream()
                .map(vehicle -> vehicleMapper.mapVehicle(
                        vehicle,
                        vehicleTypes.get(vehicle.getVehicleTypeId()),
                        pricingPlans.get(vehicle.getPricingPlanId()),
                        system
                ))
                .filter(vehicle -> vehicle.getVehicleType() != null)
                .filter(vehicle -> vehicle.getPricingPlan() != null)
                .collect(Collectors.toMap(v -> getVehicleCacheKey(v, feedProvider), v -> v));

        Set<String> spatialIndicesToRemove = new java.util.HashSet<>(Set.of());
        Map<String, Vehicle> spatialIndexUpdateMap = new java.util.HashMap<>(Map.of());

        vehicles.forEach((key, vehicle) -> {
            var spatialIndexId = SpatialIndexIdUtil.createSpatialIndexId(vehicle, feedProvider);
            var previousVehicle = originalVehicles.get(getVehicleCacheKey(vehicle, feedProvider));

            if (previousVehicle != null) {
                var oldSpatialIndexId = SpatialIndexIdUtil.createSpatialIndexId(previousVehicle, feedProvider);
                if (!oldSpatialIndexId.equalsIgnoreCase(spatialIndexId)) {
                    spatialIndicesToRemove.add(oldSpatialIndexId);
                }
            }
            spatialIndexUpdateMap.put(spatialIndexId, vehicle);
        });

        spatialIndicesToRemove.addAll(
                vehicleIdsToRemove.stream()
                        .map(vehicleId -> SpatialIndexIdUtil.createSpatialIndexId(originalVehicles.get(vehicleId), feedProvider))
                        .collect(Collectors.toSet())
        );

        if (spatialIndicesToRemove.size() > 0) {
            logger.debug("Removing {} stale entries in spatial index", spatialIndicesToRemove.size());
            spatialIndex.removeAll(spatialIndicesToRemove);
        }

        if (vehicleIdsToRemove.size() > 0) {
            logger.debug("Removing {} vehicles from vehicle cache", vehicleIdsToRemove.size());
            vehicleCache.removeAll(vehicleIdsToRemove);
        }

        if (vehicles.size() > 0) {
            logger.debug("Adding/updating {} vehicles in vechile cache", vehicles.size());
            vehicleCache.updateAll(vehicles);
        }

        if (spatialIndexUpdateMap.size() > 0) {
            logger.debug("Updating {} entries in spatial index", spatialIndexUpdateMap.size());
            spatialIndex.addAll(spatialIndexUpdateMap);
        }
    }

    private String getVehicleCacheKey(Vehicle vehicle, FeedProvider feedProvider) {
        return vehicle.getId() + "_" + feedProvider.getName();
    }
}
