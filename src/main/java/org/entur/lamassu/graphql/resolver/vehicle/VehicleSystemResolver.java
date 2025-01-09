package org.entur.lamassu.graphql.resolver.vehicle;

import org.entur.lamassu.cache.EntityCache;
import org.entur.lamassu.model.entities.System;
import org.entur.lamassu.model.entities.Vehicle;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

@Controller
public class VehicleSystemResolver {

  private final EntityCache<System> systemCache;

  public VehicleSystemResolver(EntityCache<System> systemCache) {
    this.systemCache = systemCache;
  }

  @SchemaMapping(typeName = "Vehicle", field = "system")
  public System resolve(Vehicle vehicle) {
    return systemCache.get(vehicle.getSystemId());
  }
}
