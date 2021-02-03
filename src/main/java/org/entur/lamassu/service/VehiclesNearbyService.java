package org.entur.lamassu.service;

import org.entur.lamassu.model.Vehicle;
import org.entur.lamassu.model.VehicleFilterParameters;
import org.entur.lamassu.model.VehicleQueryParameters;

import java.util.List;

public interface VehiclesNearbyService {
    List<Vehicle> getVehiclesNearby(VehicleQueryParameters vehicleQueryParameters, VehicleFilterParameters vehicleFilterParameters);
}
