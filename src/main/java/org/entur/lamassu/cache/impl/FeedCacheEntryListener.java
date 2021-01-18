package org.entur.lamassu.cache.impl;

import org.entur.lamassu.cache.PricingPlanCache;
import org.entur.lamassu.cache.VehicleCache;
import org.entur.lamassu.cache.VehicleTypeCache;
import org.entur.lamassu.mapper.PricingPlanMapper;
import org.entur.lamassu.mapper.VehicleMapper;
import org.entur.lamassu.mapper.VehicleTypeMapper;
import org.entur.lamassu.model.gbfs.v2_1.FreeBikeStatus;
import org.entur.lamassu.model.gbfs.v2_1.GBFSBase;
import org.entur.lamassu.model.gbfs.v2_1.SystemPricingPlans;
import org.entur.lamassu.model.gbfs.v2_1.VehicleTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.cache.event.CacheEntryCreatedListener;
import javax.cache.event.CacheEntryEvent;
import javax.cache.event.CacheEntryListener;
import javax.cache.event.CacheEntryUpdatedListener;
import java.io.Serializable;
import java.util.stream.Collectors;

public class FeedCacheEntryListener implements
        CacheEntryCreatedListener<String, GBFSBase>,
        CacheEntryUpdatedListener<String, GBFSBase>,
        Serializable, CacheEntryListener<String, GBFSBase> {

    private final transient Logger logger = LoggerFactory.getLogger(this.getClass());

    private final transient VehicleCache vehicleCache;
    private final transient VehicleMapper vehicleMapper;
    private final transient VehicleTypeCache vehicleTypeCache;
    private final transient VehicleTypeMapper vehicleTypeMapper;
    private final transient PricingPlanCache pricingPlanCache;
    private final transient PricingPlanMapper pricingPlanMapper;

    public FeedCacheEntryListener(VehicleCache vehicleCache, VehicleMapper vehicleMapper, VehicleTypeCache vehicleTypeCache, VehicleTypeMapper vehicleTypeMapper, PricingPlanCache pricingPlanCache, PricingPlanMapper pricingPlanMapper) {
        this.vehicleCache = vehicleCache;
        this.vehicleMapper = vehicleMapper;
        this.vehicleTypeCache = vehicleTypeCache;
        this.vehicleTypeMapper = vehicleTypeMapper;
        this.pricingPlanCache = pricingPlanCache;
        this.pricingPlanMapper = pricingPlanMapper;
    }

    @Override
    public void onCreated(Iterable<CacheEntryEvent<? extends String, ? extends GBFSBase>> iterable) {
        updateVehicles(iterable);
    }

    @Override
    public void onUpdated(Iterable<CacheEntryEvent<? extends String, ? extends GBFSBase>> iterable) {
        updateVehicles(iterable);
    }

    private void updateVehicles(Iterable<CacheEntryEvent<? extends String, ? extends GBFSBase>> iterable) {
        if (iterable != null) {
            for (var entry : iterable) {
                if (entry.getValue() instanceof FreeBikeStatus) {
                    var freeBikeStatusFeed = (FreeBikeStatus) entry.getValue();
                    try {
                        var vehicles = freeBikeStatusFeed.getData().getBikes().stream()
                                .map(vehicle -> vehicleMapper.mapVehicle(
                                        vehicle,
                                        vehicleTypeCache.get(vehicle.getVehicleTypeId()),
                                        pricingPlanCache.get(vehicle.getPricingPlanId())
                                ))
                                .collect(Collectors.toList());
                        vehicleCache.updateAll(vehicles);
                        logger.info("Added vehicles to vehicle cache from feed {}", entry.getKey());
                    } catch (NullPointerException e) {
                        logger.warn("Caught NullPointerException when updating vehicle cache from freeBikeStatusFeed: {}", freeBikeStatusFeed, e);
                    }
                } else if (entry.getValue() instanceof VehicleTypes) {
                    var vehicleTypesFeed = (VehicleTypes) entry.getValue();
                    try {
                        var vehicleTypes = vehicleTypesFeed.getData().getVehicleTypes().stream()
                                .map(vehicleTypeMapper::mapVehicleType).collect(Collectors.toList());
                        vehicleTypeCache.updateAll(vehicleTypes);
                        logger.info("Added vehicle types to vehicle types cache from feed {}", entry.getKey());
                    } catch (NullPointerException e) {
                        logger.warn("Caught NullPointerException when updating vehicle type cache from vehicleTypesFeed: {}", vehicleTypesFeed, e);
                    }
                } else if (entry.getValue() instanceof SystemPricingPlans) {
                    var pricingPlansFeed = (SystemPricingPlans) entry.getValue();
                    try {
                        var pricingPlans = pricingPlansFeed.getData().getPlans().stream()
                                .map(pricingPlanMapper::mapPricingPlan).collect(Collectors.toList());
                        pricingPlanCache.updateAll(pricingPlans);
                        logger.info("Added pricing plans to pricing plan cache from feed {}", entry.getKey());
                    } catch (NullPointerException e) {
                        logger.warn("Caught NullPointerException when updating pricing plan cache from pricingPlansFeed: {}", pricingPlansFeed, e);
                    }
                }
            }
        }
    }
}
