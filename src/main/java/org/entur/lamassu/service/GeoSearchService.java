package org.entur.lamassu.service;

import org.entur.lamassu.model.entities.Station;
import org.entur.lamassu.model.entities.Vehicle;

import java.util.List;

public interface GeoSearchService {
    List<Vehicle> getVehiclesNearby(RangeQueryParameters rangeQueryParameters, VehicleFilterParameters vehicleFilterParameters);
    List<Station> getStationsNearby(RangeQueryParameters rangeQueryParameters, FilterParameters stationFilterParameters);
}
