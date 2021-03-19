package org.entur.lamassu.updater;

import org.entur.lamassu.listener.CacheListener;
import org.entur.lamassu.model.entities.Vehicle;
import org.entur.lamassu.model.gbfs.v2_1.FreeBikeStatus;
import org.entur.lamassu.model.gbfs.v2_1.SystemInformation;
import org.entur.lamassu.model.gbfs.v2_1.SystemPricingPlans;
import org.entur.lamassu.model.gbfs.v2_1.VehicleTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ListenerManager {
    private final CacheListener<FreeBikeStatus> freeBikeStatusCacheListener;
    private final CacheListener<VehicleTypes> vehicleTypesCacheListener;
    private final CacheListener<SystemPricingPlans> systemPricingPlansCacheListener;
    private final CacheListener<SystemInformation> systemInformationCacheListener;
    private final CacheListener<Vehicle> vehicleCacheListener;

    @Autowired
    public ListenerManager(
            CacheListener<FreeBikeStatus> freeBikeStatusCacheListener,
            CacheListener<VehicleTypes> vehicleTypesCacheListener,
            CacheListener<SystemPricingPlans> systemPricingPlansCacheListener,
            CacheListener<SystemInformation> systemInformationCacheListener,
            CacheListener<Vehicle> vehicleCacheListener
    ) {
        this.freeBikeStatusCacheListener = freeBikeStatusCacheListener;
        this.vehicleTypesCacheListener = vehicleTypesCacheListener;
        this.systemPricingPlansCacheListener = systemPricingPlansCacheListener;
        this.systemInformationCacheListener = systemInformationCacheListener;
        this.vehicleCacheListener = vehicleCacheListener;
    }

    public void start() {
        freeBikeStatusCacheListener.startListening();
        vehicleTypesCacheListener.startListening();
        systemPricingPlansCacheListener.startListening();
        systemInformationCacheListener.startListening();
        vehicleCacheListener.startListening();
    }

    public void stop() {
        freeBikeStatusCacheListener.stopListening();
        vehicleTypesCacheListener.stopListening();
        systemPricingPlansCacheListener.stopListening();
        systemInformationCacheListener.stopListening();
        vehicleCacheListener.stopListening();
    }
}
