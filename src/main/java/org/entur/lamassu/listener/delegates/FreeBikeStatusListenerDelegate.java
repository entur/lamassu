package org.entur.lamassu.listener.delegates;

import org.entur.lamassu.cache.PricingPlanCache;
import org.entur.lamassu.cache.VehicleCache;
import org.entur.lamassu.cache.VehicleTypeCache;
import org.entur.lamassu.config.feedprovider.FeedProviderConfig;
import org.entur.lamassu.listener.CacheEntryListenerDelegate;
import org.entur.lamassu.mapper.VehicleMapper;
import org.entur.lamassu.model.gbfs.v2_1.FreeBikeStatus;
import org.entur.lamassu.model.gbfs.v2_1.GBFSBase;
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
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public FreeBikeStatusListenerDelegate(
            VehicleMapper vehicleMapper,
            VehicleCache vehicleCache,
            VehicleTypeCache vehicleTypeCache,
            PricingPlanCache pricingPlanCache
    ) {
        this.vehicleMapper = vehicleMapper;
        this.vehicleCache = vehicleCache;
        this.vehicleTypeCache = vehicleTypeCache;
        this.pricingPlanCache = pricingPlanCache;
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
        var providerName = split[split.length - 1];
        var freeBikeStatusFeed = (FreeBikeStatus) event.getValue();
        try {
            var vehicles = freeBikeStatusFeed.getData().getBikes().stream()
                    .map(vehicle -> vehicleMapper.mapVehicle(
                            vehicle,
                            vehicleTypeCache.get(vehicle.getVehicleTypeId()),
                            pricingPlanCache.get(vehicle.getPricingPlanId())
                    ))
                    .filter(vehicle -> vehicle.getVehicleType() != null)
                    .filter(vehicle -> vehicle.getPricingPlan() != null)
                    .distinct()
                    .collect(Collectors.toMap(v -> providerName + "_" + v.getId(), v -> v));
            vehicleCache.updateAll(vehicles);
            logger.info("Added vehicles to vehicle cache from feed {}", event.getKey());
        } catch (NullPointerException e) {
            logger.warn("Caught NullPointerException when updating vehicle cache from freeBikeStatusFeed: {}", freeBikeStatusFeed, e);
        }
    }
}
