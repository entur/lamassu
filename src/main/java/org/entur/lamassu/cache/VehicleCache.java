package org.entur.lamassu.cache;

import org.entur.lamassu.model.Vehicle;

public interface VehicleCache extends EntityCache<Vehicle> {
    void startListening();
    void stopListening();
}
