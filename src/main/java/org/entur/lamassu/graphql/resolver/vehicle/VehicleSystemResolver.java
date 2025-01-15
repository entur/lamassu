package org.entur.lamassu.graphql.resolver.vehicle;

import org.entur.lamassu.cache.EntityReader;
import org.entur.lamassu.model.entities.System;
import org.entur.lamassu.model.entities.Vehicle;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

@Controller
public class VehicleSystemResolver {

  private final EntityReader<System> systemReader;

  public VehicleSystemResolver(EntityReader<System> systemReader) {
    this.systemReader = systemReader;
  }

  @SchemaMapping(typeName = "Vehicle", field = "system")
  public System resolve(Vehicle vehicle) {
    return systemReader.get(vehicle.getSystemId());
  }
}
