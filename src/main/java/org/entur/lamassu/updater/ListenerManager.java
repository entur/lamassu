package org.entur.lamassu.updater;

import org.entur.lamassu.listener.CacheListener;
import org.entur.lamassu.model.entities.Vehicle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ListenerManager {
    private final CacheListener<Vehicle> vehicleCacheListener;


    @Autowired
    public ListenerManager(
            CacheListener<Vehicle> vehicleCacheListener
    ) {
        this.vehicleCacheListener = vehicleCacheListener;
    }

    public void start() {
        vehicleCacheListener.startListening();
    }

    public void stop() {
        vehicleCacheListener.stopListening();
    }
}
