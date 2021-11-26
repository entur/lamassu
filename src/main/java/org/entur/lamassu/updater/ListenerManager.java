package org.entur.lamassu.updater;

import org.entur.lamassu.listener.CacheListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ListenerManager {
    private final CacheListener vehicleCacheListener;


    @Autowired
    public ListenerManager(
            CacheListener vehicleCacheListener
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
