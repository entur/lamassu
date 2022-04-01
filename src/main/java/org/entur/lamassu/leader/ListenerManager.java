package org.entur.lamassu.leader;

import org.entur.lamassu.leader.listener.CacheListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("leader")
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
