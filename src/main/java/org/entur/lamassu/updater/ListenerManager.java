package org.entur.lamassu.updater;

import org.entur.lamassu.listener.CacheListener;
import org.entur.lamassu.model.entities.Vehicle;
import org.entur.lamassu.model.gbfs.v2_1.FreeBikeStatus;
import org.entur.lamassu.model.gbfs.v2_1.SystemPricingPlans;
import org.entur.lamassu.model.gbfs.v2_1.VehicleTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ListenerManager {
    private final CacheListener<FreeBikeStatus> freeBikeStatusFeedCacheListener;
    private final CacheListener<VehicleTypes> vehicleTypesFeedCacheListener;
    private final CacheListener<SystemPricingPlans> systemPricingPlansFeedCacheListener;
    private final CacheListener<Vehicle> vehicleCacheListener;

    @Autowired
    public ListenerManager(
            CacheListener<FreeBikeStatus> freeBikeStatusFeedCacheListener,
            CacheListener<VehicleTypes> vehicleTypesFeedCacheListener,
            CacheListener<SystemPricingPlans> systemPricingPlansFeedCacheListener,
            CacheListener<Vehicle> vehicleCacheListener
    ) {
        this.freeBikeStatusFeedCacheListener = freeBikeStatusFeedCacheListener;
        this.vehicleTypesFeedCacheListener = vehicleTypesFeedCacheListener;
        this.systemPricingPlansFeedCacheListener = systemPricingPlansFeedCacheListener;
        this.vehicleCacheListener = vehicleCacheListener;
    }

    public void start() {
        freeBikeStatusFeedCacheListener.startListening();
        vehicleTypesFeedCacheListener.startListening();
        systemPricingPlansFeedCacheListener.startListening();
        vehicleCacheListener.startListening();
    }

    public void stop() {
        freeBikeStatusFeedCacheListener.stopListening();
        vehicleTypesFeedCacheListener.stopListening();
        systemPricingPlansFeedCacheListener.stopListening();
        vehicleCacheListener.stopListening();
    }
}
