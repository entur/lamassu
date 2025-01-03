package org.entur.lamassu.graphql.controller;

import org.entur.lamassu.cache.SystemCache;
import org.entur.lamassu.model.entities.Station;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

@Controller
public class StationSystemFieldGraphQLController {

  private final SystemCache systemCache;

  public StationSystemFieldGraphQLController(SystemCache systemCache) {
    this.systemCache = systemCache;
  }

  @SchemaMapping(typeName = "Station", field = "system")
  public org.entur.lamassu.model.entities.System getSystem(Station station) {
    return systemCache.get(station.getSystemId());
  }
}
