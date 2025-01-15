package org.entur.lamassu.graphql.resolver.station;

import org.entur.lamassu.cache.EntityReader;
import org.entur.lamassu.model.entities.Station;
import org.entur.lamassu.model.entities.System;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

@Controller
public class StationSystemResolver {

  private final EntityReader<System> systemReader;

  public StationSystemResolver(EntityReader<System> systemReader) {
    this.systemReader = systemReader;
  }

  @SchemaMapping(typeName = "Station", field = "system")
  public System resolve(Station station) {
    return systemReader.get(station.getSystemId());
  }
}
