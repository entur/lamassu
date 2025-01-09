package org.entur.lamassu.graphql.resolver.vehicle;

import org.entur.lamassu.cache.EntityCache;
import org.entur.lamassu.model.entities.Vehicle;
import org.entur.lamassu.model.entities.VehicleType;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

@Controller
public class VehicleTypeResolver {

  private final EntityCache<VehicleType> vehicleTypeCache;

  public VehicleTypeResolver(EntityCache<VehicleType> vehicleTypeCache) {
    this.vehicleTypeCache = vehicleTypeCache;
  }

  @SchemaMapping(typeName = "Vehicle", field = "vehicleType")
  public VehicleType resolve(Vehicle vehicle) {
    return vehicleTypeCache.get(vehicle.getVehicleTypeId());
  }
}
