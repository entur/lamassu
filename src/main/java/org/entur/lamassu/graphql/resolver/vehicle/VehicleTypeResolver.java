package org.entur.lamassu.graphql.resolver.vehicle;

import org.entur.lamassu.cache.EntityReader;
import org.entur.lamassu.model.entities.Vehicle;
import org.entur.lamassu.model.entities.VehicleType;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

@Controller
public class VehicleTypeResolver {

  private final EntityReader<VehicleType> vehicleTypeReader;

  public VehicleTypeResolver(EntityReader<VehicleType> vehicleTypeReader) {
    this.vehicleTypeReader = vehicleTypeReader;
  }

  @SchemaMapping(typeName = "Vehicle", field = "vehicleType")
  public VehicleType resolve(Vehicle vehicle) {
    return vehicleTypeReader.get(vehicle.getVehicleTypeId());
  }
}
