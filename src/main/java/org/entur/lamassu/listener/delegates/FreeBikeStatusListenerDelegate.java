package org.entur.lamassu.listener.delegates;

import org.entur.lamassu.cache.GBFSFeedCache;
import org.entur.lamassu.cache.VehicleCache;
import org.entur.lamassu.cache.VehicleSpatialIndex;
import org.entur.lamassu.listener.CacheEntryListenerDelegate;
import org.entur.lamassu.mapper.PricingPlanMapper;
import org.entur.lamassu.mapper.SystemMapper;
import org.entur.lamassu.mapper.VehicleMapper;
import org.entur.lamassu.mapper.VehicleTypeMapper;
import org.entur.lamassu.model.entities.PricingPlan;
import org.entur.lamassu.model.entities.Vehicle;
import org.entur.lamassu.model.entities.VehicleType;
import org.entur.lamassu.model.discovery.FeedProvider;
import org.entur.lamassu.model.gbfs.v2_1.FreeBikeStatus;
import org.entur.lamassu.model.gbfs.v2_1.GBFSBase;
import org.entur.lamassu.model.gbfs.v2_1.GBFSFeedName;
import org.entur.lamassu.model.gbfs.v2_1.SystemInformation;
import org.entur.lamassu.model.gbfs.v2_1.SystemPricingPlans;
import org.entur.lamassu.model.gbfs.v2_1.VehicleTypes;
import org.entur.lamassu.service.FeedProviderService;
import org.entur.lamassu.util.SpatialIndexIdUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.cache.event.CacheEntryEvent;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class FreeBikeStatusListenerDelegate implements CacheEntryListenerDelegate<GBFSBase, FreeBikeStatus> {
    private final VehicleCache vehicleCache;
    private final GBFSFeedCache feedCache;
    private final FeedProviderService feedProviderService;
    private final VehicleSpatialIndex spatialIndex;
    private final SystemMapper systemMapper;
    private final PricingPlanMapper pricingPlanMapper;
    private final VehicleTypeMapper vehicleTypeMapper;
    private final VehicleMapper vehicleMapper;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public FreeBikeStatusListenerDelegate(
            VehicleCache vehicleCache,
            GBFSFeedCache feedCache,
            FeedProviderService feedProviderService,
            VehicleSpatialIndex spatialIndex,
            VehicleMapper vehicleMapper,
            SystemMapper systemMapper,
            PricingPlanMapper pricingPlanMapper,
            VehicleTypeMapper vehicleTypeMapper
    ) {
        this.vehicleCache = vehicleCache;
        this.feedCache = feedCache;
        this.feedProviderService = feedProviderService;
        this.spatialIndex = spatialIndex;
        this.vehicleMapper = vehicleMapper;
        this.systemMapper = systemMapper;
        this.pricingPlanMapper = pricingPlanMapper;
        this.vehicleTypeMapper = vehicleTypeMapper;
    }

    @Override
    public void onCreated(Iterable<CacheEntryEvent<? extends String, ? extends GBFSBase>> iterable) {
        for (var event : iterable) {
            addOrUpdateVehicles(event);
        }
    }

    @Override
    public void onUpdated(Iterable<CacheEntryEvent<? extends String, ? extends GBFSBase>> iterable) {
        for (var event : iterable) {
            addOrUpdateVehicles(event);
        }
    }

    @Override
    public void onRemoved(Iterable<CacheEntryEvent<? extends String, ? extends GBFSBase>> iterable) {
        // noop
    }

    @Override
    public void onExpired(Iterable<CacheEntryEvent<? extends String, ? extends GBFSBase>> iterable) {
        // noop
    }

    private void addOrUpdateVehicles(CacheEntryEvent<? extends String, ? extends GBFSBase> event) {
        var split = event.getKey().split("_");
        var feedProvider = feedProviderService.getFeedProviderBySystemSlug(split[split.length - 1]);
        var freeBikeStatusFeed = (FreeBikeStatus) event.getValue();

        var systemInformationFeed = (SystemInformation) feedCache.find(GBFSFeedName.SYSTEM_INFORMATION, feedProvider);
        var pricingPlansFeed = (SystemPricingPlans) feedCache.find(GBFSFeedName.SYSTEM_PRICING_PLANS, feedProvider);
        var vehicleTypesFeed = (VehicleTypes) feedCache.find(GBFSFeedName.VEHICLE_TYPES, feedProvider);

        var vehicleIds = freeBikeStatusFeed.getData().getBikes().stream()
                .map(FreeBikeStatus.Bike::getBikeId)
                .collect(Collectors.toSet());

        Set<String> vehicleIdsToRemove = new java.util.HashSet<>(Set.of());

        if (event.isOldValueAvailable()) {
            var oldFreeBikeStatusFeed = (FreeBikeStatus) event.getOldValue();
            vehicleIdsToRemove = oldFreeBikeStatusFeed.getData().getBikes().stream()
                    .map(FreeBikeStatus.Bike::getBikeId).collect(Collectors.toSet());

            // Find vehicle ids in old feed not present in new feed
            vehicleIdsToRemove.removeAll(vehicleIds);
            logger.debug("Found {} vehicleIds to remove from old free_bike_status feed: {}", vehicleIdsToRemove.size());

            // Add vehicle ids that are staged for removal to the set of vehicle ids that will be used to
            // fetch current vehicles from cache
            vehicleIds.addAll(vehicleIdsToRemove);
        }

        var currentVehicles = vehicleCache.getAllAsMap(
                vehicleIds.stream()
                        .map(id -> getVehicleCacheKey(id, feedProvider))
                        .collect(Collectors.toSet())
        );

        var vehicleTypes = vehicleTypesFeed.getData().getVehicleTypes().stream()
                .map(vehicleType -> vehicleTypeMapper.mapVehicleType(vehicleType, feedProvider.getLanguage()))
                .collect(Collectors.toMap(VehicleType::getId, i -> i));
        var system = systemMapper.mapSystem(systemInformationFeed.getData(), feedProvider);
        var pricingPlans = pricingPlansFeed.getData().getPlans().stream()
                .map(pricingPlan -> pricingPlanMapper.mapPricingPlan(pricingPlan, feedProvider.getLanguage()))
                .collect(Collectors.toMap(PricingPlan::getId, i -> i));

        var vehicles = freeBikeStatusFeed.getData().getBikes().stream()
                .map(vehicle -> vehicleMapper.mapVehicle(
                        vehicle,
                        vehicleTypes.get(vehicle.getVehicleTypeId()),
                        pricingPlans.get(vehicle.getPricingPlanId()),
                        system
                ))
                .collect(Collectors.toMap(v -> getVehicleCacheKey(v.getId(), feedProvider), v -> v));

        Set<String> spatialIndicesToRemove = new java.util.HashSet<>(Set.of());
        Map<String, Vehicle> spatialIndexUpdateMap = new java.util.HashMap<>(Map.of());

        vehicles.forEach((key, vehicle) -> {
            var spatialIndexId = SpatialIndexIdUtil.createVehicleSpatialIndexId(vehicle, feedProvider);
            var previousVehicle = currentVehicles.get(key);

            if (previousVehicle != null) {
                var oldSpatialIndexId = SpatialIndexIdUtil.createVehicleSpatialIndexId(previousVehicle, feedProvider);
                if (!oldSpatialIndexId.equalsIgnoreCase(spatialIndexId)) {
                    spatialIndicesToRemove.add(oldSpatialIndexId);
                }
            }
            spatialIndexUpdateMap.put(spatialIndexId, vehicle);
        });

        spatialIndicesToRemove.addAll(
                vehicleIdsToRemove.stream()
                        .map(vehicleId -> getVehicleCacheKey(vehicleId, feedProvider))
                        .filter(currentVehicles::containsKey)
                        .map(vehicleCacheKey -> SpatialIndexIdUtil.createVehicleSpatialIndexId(currentVehicles.get(vehicleCacheKey), feedProvider))
                        .collect(Collectors.toSet())
        );

        if (!spatialIndicesToRemove.isEmpty()) {
            logger.debug("Removing {} stale entries in spatial index", spatialIndicesToRemove.size());
            spatialIndex.removeAll(spatialIndicesToRemove);
        }

        if (!vehicleIdsToRemove.isEmpty()) {
            logger.debug("Removing {} vehicles from vehicle cache", vehicleIdsToRemove.size());
            vehicleCache.removeAll(vehicleIdsToRemove.stream().map(id -> getVehicleCacheKey(id, feedProvider)).collect(Collectors.toSet()));
        }

        if (!vehicles.isEmpty()) {
            logger.debug("Adding/updating {} vehicles in vehicle cache", vehicles.size());
            vehicleCache.updateAll(vehicles);
        }

        if (!spatialIndexUpdateMap.isEmpty()) {
            logger.debug("Updating {} entries in spatial index", spatialIndexUpdateMap.size());
            spatialIndex.addAll(spatialIndexUpdateMap);
        }
    }


    private String getVehicleCacheKey(String vehicleId, FeedProvider feedProvider) {
        return vehicleId + "_" + feedProvider.getSystemSlug();
    }
}
