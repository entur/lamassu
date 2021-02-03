package org.entur.lamassu.service;

import org.entur.lamassu.model.Vehicle;

import java.util.List;

public interface VehiclesNearbyService {
    List<Vehicle> getVehiclesNearby(VehicleQueryParameters vehicleQueryParameters, VehicleFilterParameters vehicleFilterParameters);
}
