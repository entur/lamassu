package org.entur.lamassu.graphql.controller;

import org.entur.lamassu.cache.SystemCache;
import org.entur.lamassu.model.entities.Vehicle;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

@Controller
public class VehicleSystemFieldGraphQLController {

  private final SystemCache systemCache;

  public VehicleSystemFieldGraphQLController(SystemCache systemCache) {
    this.systemCache = systemCache;
  }

  @SchemaMapping(typeName = "Vehicle", field = "system")
  public org.entur.lamassu.model.entities.System system(Vehicle vehicle) {
    return systemCache.get(vehicle.getSystemId());
  }
}
