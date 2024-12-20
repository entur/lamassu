package org.entur.lamassu.util;

import java.util.List;
import org.entur.lamassu.cache.StationSpatialIndexId;
import org.entur.lamassu.cache.VehicleSpatialIndexId;
import org.entur.lamassu.model.entities.Station;
import org.entur.lamassu.model.entities.Vehicle;
import org.entur.lamassu.model.provider.FeedProvider;

/**
 * Test helper class for creating spatial index IDs.
 * This replaces the functionality of SpatialIndexService in tests.
 */
public class TestSpatialIndexBuilder {

  private TestSpatialIndexBuilder() {}

  public static VehicleSpatialIndexId createVehicleIndex(
    Vehicle vehicle,
    FeedProvider provider
  ) {
    var id = new VehicleSpatialIndexId();
    id.setId(vehicle.getId());
    id.setCodespace(provider.getCodespace());
    id.setSystemId(provider.getSystemId());
    id.setOperatorId(provider.getOperatorId());
    id.setFormFactor(vehicle.getVehicleType().getFormFactor());
    id.setPropulsionType(vehicle.getVehicleType().getPropulsionType());
    id.setReserved(vehicle.getReserved());
    id.setDisabled(vehicle.getDisabled());
    return id;
  }

  public static StationSpatialIndexId createStationIndex(
    Station station,
    FeedProvider provider
  ) {
    var id = new StationSpatialIndexId();
    id.setId(station.getId());
    id.setCodespace(provider.getCodespace());
    id.setSystemId(provider.getSystemId());
    id.setOperatorId(provider.getOperatorId());

    if (station.getVehicleTypesAvailable() != null) {
      id.setAvailableFormFactors(
        station
          .getVehicleTypesAvailable()
          .stream()
          .map(vta -> vta.getVehicleType().getFormFactor())
          .toList()
      );
      id.setAvailablePropulsionTypes(
        station
          .getVehicleTypesAvailable()
          .stream()
          .map(vta -> vta.getVehicleType().getPropulsionType())
          .toList()
      );
    } else {
      id.setAvailableFormFactors(List.of());
      id.setAvailablePropulsionTypes(List.of());
    }

    return id;
  }
}
