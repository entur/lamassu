package org.entur.lamassu.service;

import java.util.Collection;
import java.util.List;
import org.entur.lamassu.model.entities.Station;
import org.entur.lamassu.model.entities.Vehicle;

public interface GeoSearchService {
  List<Vehicle> getVehiclesWithinRange(
    RangeQueryParameters rangeQueryParameters,
    VehicleFilterParameters vehicleFilterParameters
  );
  List<Vehicle> getVehiclesInBoundingBox(
    BoundingBoxQueryParameters boundingBoxQueryParameters,
    VehicleFilterParameters vehicleFilterParameters
  );
  List<Station> getStationsWithinRange(
    RangeQueryParameters rangeQueryParameters,
    StationFilterParameters stationFilterParameters
  );
  List<Station> getStationsInBoundingBox(
    BoundingBoxQueryParameters boundingBoxQueryParameters,
    StationFilterParameters stationFilterParameters
  );
  Collection<String> getVehicleSpatialIndexOrphans();
  Collection<String> removeVehicleSpatialIndexOrphans();
}
