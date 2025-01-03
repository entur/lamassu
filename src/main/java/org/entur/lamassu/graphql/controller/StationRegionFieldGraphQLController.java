package org.entur.lamassu.graphql.controller;

import org.entur.lamassu.cache.RegionCache;
import org.entur.lamassu.model.entities.Region;
import org.entur.lamassu.model.entities.Station;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

@Controller
public class StationRegionFieldGraphQLController {

  private final RegionCache regionCache;

  public StationRegionFieldGraphQLController(RegionCache regionCache) {
    this.regionCache = regionCache;
  }

  @SchemaMapping(typeName = "Station", field = "region")
  public Region getRegion(Station station) {
    if (station.getRegionId() == null) {
      return null;
    }
    return regionCache.get(station.getRegionId());
  }
}
