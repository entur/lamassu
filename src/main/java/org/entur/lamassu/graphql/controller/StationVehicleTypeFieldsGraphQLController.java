package org.entur.lamassu.graphql.controller;

import java.util.HashSet;
import java.util.List;
import org.entur.lamassu.cache.VehicleTypeCache;
import org.entur.lamassu.model.entities.VehicleDocksAvailability;
import org.entur.lamassu.model.entities.VehicleDocksCapacity;
import org.entur.lamassu.model.entities.VehicleType;
import org.entur.lamassu.model.entities.VehicleTypeAvailability;
import org.entur.lamassu.model.entities.VehicleTypeCapacity;
import org.entur.lamassu.model.entities.VehicleTypesCapacity;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

@Controller
public class StationVehicleTypeFieldsGraphQLController extends BaseGraphQLController {

  private final VehicleTypeCache vehicleTypeCache;

  public StationVehicleTypeFieldsGraphQLController(VehicleTypeCache vehicleTypeCache) {
    this.vehicleTypeCache = vehicleTypeCache;
  }

  @SchemaMapping(typeName = "VehicleTypeAvailability", field = "vehicleType")
  public VehicleType getVehicleType(VehicleTypeAvailability vehicleTypeAvailability) {
    return vehicleTypeCache.get(vehicleTypeAvailability.getVehicleTypeId());
  }

  @SchemaMapping(typeName = "VehicleDocksAvailability", field = "vehicleTypes")
  public List<VehicleType> getVehicleType(
    VehicleDocksAvailability vehicleDocksAvailability
  ) {
    return vehicleTypeCache.getAll(
      new HashSet<>(vehicleDocksAvailability.getVehicleTypeIds())
    );
  }

  @SchemaMapping(typeName = "VehicleTypeCapacity", field = "vehicleType")
  public VehicleType getVehicleType(VehicleTypeCapacity vehicleTypeCapacity) {
    return vehicleTypeCache.get(vehicleTypeCapacity.getVehicleTypeId());
  }

  @SchemaMapping(typeName = "VehicleTypesCapacity", field = "vehicleTypes")
  public List<VehicleType> getVehicleType(VehicleTypesCapacity vehicleTypesCapacity) {
    return vehicleTypeCache.getAll(
      new HashSet<>(vehicleTypesCapacity.getVehicleTypeIds())
    );
  }

  @SchemaMapping(typeName = "VehicleDocksCapacity", field = "vehicleTypes")
  public List<VehicleType> getVehicleType(VehicleDocksCapacity vehicleDocksCapacity) {
    return vehicleTypeCache.getAll(
      new HashSet<>(vehicleDocksCapacity.getVehicleTypeIds())
    );
  }
}
