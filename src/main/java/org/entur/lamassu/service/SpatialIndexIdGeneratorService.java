package org.entur.lamassu.service;

import java.util.List;
import java.util.stream.Collectors;
import org.entur.lamassu.cache.EntityCache;
import org.entur.lamassu.cache.StationSpatialIndexId;
import org.entur.lamassu.cache.VehicleSpatialIndexId;
import org.entur.lamassu.model.entities.Station;
import org.entur.lamassu.model.entities.Vehicle;
import org.entur.lamassu.model.entities.VehicleType;
import org.entur.lamassu.model.entities.VehicleTypeAvailability;
import org.entur.lamassu.model.provider.FeedProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SpatialIndexIdGeneratorService {

  private final EntityCache<VehicleType> vehicleTypeCache;

  @Autowired
  public SpatialIndexIdGeneratorService(EntityCache<VehicleType> vehicleTypeCache) {
    this.vehicleTypeCache = vehicleTypeCache;
  }

  public VehicleSpatialIndexId createVehicleIndexId(
    Vehicle vehicle,
    FeedProvider provider
  ) {
    VehicleType vehicleType = vehicleTypeCache.get(vehicle.getVehicleTypeId());
    if (vehicleType == null) {
      throw new IllegalStateException(
        "Vehicle type not found for id: " + vehicle.getVehicleTypeId()
      );
    }

    var id = new VehicleSpatialIndexId();
    id.setId(vehicle.getId());
    id.setCodespace(provider.getCodespace());
    id.setSystemId(provider.getSystemId());
    id.setOperatorId(provider.getOperatorId());
    id.setFormFactor(vehicleType.getFormFactor());
    id.setPropulsionType(vehicleType.getPropulsionType());
    id.setReserved(vehicle.getReserved());
    id.setDisabled(vehicle.getDisabled());
    return id;
  }

  public StationSpatialIndexId createStationIndexId(
    Station station,
    FeedProvider provider
  ) {
    var id = new StationSpatialIndexId();
    id.setId(station.getId());
    id.setCodespace(provider.getCodespace());
    id.setSystemId(provider.getSystemId());
    id.setOperatorId(provider.getOperatorId());

    if (station.getVehicleTypesAvailable() != null) {
      var vehicleTypeIds = station
        .getVehicleTypesAvailable()
        .stream()
        .map(VehicleTypeAvailability::getVehicleTypeId)
        .collect(Collectors.toSet());

      List<VehicleType> vehicleTypes = vehicleTypeCache.getAll(vehicleTypeIds);

      id.setAvailableFormFactors(
        vehicleTypes.stream().map(VehicleType::getFormFactor).toList()
      );
      id.setAvailablePropulsionTypes(
        vehicleTypes.stream().map(VehicleType::getPropulsionType).toList()
      );
    } else {
      id.setAvailableFormFactors(List.of());
      id.setAvailablePropulsionTypes(List.of());
    }

    return id;
  }
}
