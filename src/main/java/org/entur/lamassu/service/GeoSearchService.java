package org.entur.lamassu.service;

import java.util.Collection;
import java.util.List;
import org.entur.lamassu.model.entities.Station;
import org.entur.lamassu.model.entities.Vehicle;

public interface GeoSearchService {
  List<Vehicle> getVehiclesNearby(
    RangeQueryParameters rangeQueryParameters,
    VehicleFilterParameters vehicleFilterParameters
  );
  List<Station> getStationsNearby(
    RangeQueryParameters rangeQueryParameters,
    StationFilterParameters stationFilterParameters
  );
  Collection<String> getVehicleSpatialIndexOrphans();
  Collection<String> removeVehicleSpatialIndexOrphans();
}
