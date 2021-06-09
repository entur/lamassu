package org.entur.lamassu.updater;

import org.entur.lamassu.listener.CacheListener;
import org.entur.lamassu.model.entities.Vehicle;
import org.entur.lamassu.model.gbfs.v2_1.FreeBikeStatus;
import org.entur.lamassu.model.gbfs.v2_1.GeofencingZones;
import org.entur.lamassu.model.gbfs.v2_1.StationStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ListenerManager {
    private final CacheListener<FreeBikeStatus> freeBikeStatusCacheListener;
    private final CacheListener<StationStatus> stationStatusCacheListener;
    private final CacheListener<GeofencingZones> geofencingZonesCacheListener;
    private final CacheListener<Vehicle> vehicleCacheListener;


    @Autowired
    public ListenerManager(
            CacheListener<FreeBikeStatus> freeBikeStatusCacheListener,
            CacheListener<StationStatus> stationStatusCacheListener,
            CacheListener<GeofencingZones> geofencingZonesCacheListener,
            CacheListener<Vehicle> vehicleCacheListener
    ) {
        this.freeBikeStatusCacheListener = freeBikeStatusCacheListener;
        this.stationStatusCacheListener = stationStatusCacheListener;
        this.geofencingZonesCacheListener = geofencingZonesCacheListener;
        this.vehicleCacheListener = vehicleCacheListener;
    }

    public void start() {
        freeBikeStatusCacheListener.startListening();
        stationStatusCacheListener.startListening();
        geofencingZonesCacheListener.startListening();
        vehicleCacheListener.startListening();
    }

    public void stop() {
        freeBikeStatusCacheListener.stopListening();
        stationStatusCacheListener.stopListening();
        geofencingZonesCacheListener.stopListening();
        vehicleCacheListener.stopListening();
    }
}
