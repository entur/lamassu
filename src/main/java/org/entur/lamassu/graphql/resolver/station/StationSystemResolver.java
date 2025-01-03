package org.entur.lamassu.graphql.resolver.station;

import org.entur.lamassu.cache.SystemCache;
import org.entur.lamassu.model.entities.Station;
import org.entur.lamassu.model.entities.System;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

@Controller
public class StationSystemResolver {

  private final SystemCache systemCache;

  public StationSystemResolver(SystemCache systemCache) {
    this.systemCache = systemCache;
  }

  @SchemaMapping(typeName = "Station", field = "system")
  public System resolve(Station station) {
    return systemCache.get(station.getSystemId());
  }
}
