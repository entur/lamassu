package org.entur.lamassu.graphql.controller;

import org.entur.lamassu.cache.VehicleTypeCache;
import org.entur.lamassu.model.entities.Vehicle;
import org.entur.lamassu.model.entities.VehicleType;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

@Controller
public class VehicleVehicleTypeFieldGraphQLController {

  private final VehicleTypeCache vehicleTypeCache;

  public VehicleVehicleTypeFieldGraphQLController(VehicleTypeCache vehicleTypeCache) {
    this.vehicleTypeCache = vehicleTypeCache;
  }

  @SchemaMapping(typeName = "Vehicle", field = "vehicleType")
  public VehicleType vehicleType(Vehicle vehicle) {
    return vehicleTypeCache.get(vehicle.getVehicleTypeId());
  }
}
