package org.entur.lamassu.service;

import org.entur.lamassu.model.Vehicle;

import java.util.List;

public interface VehiclesNearbyService {
    List<Vehicle> getVehiclesNearby(Double longitude, Double latitude, Double range, Integer count);
}
