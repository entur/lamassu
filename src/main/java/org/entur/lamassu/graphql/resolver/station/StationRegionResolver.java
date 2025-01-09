package org.entur.lamassu.graphql.resolver.station;

import org.entur.lamassu.cache.EntityCache;
import org.entur.lamassu.model.entities.Region;
import org.entur.lamassu.model.entities.Station;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

@Controller
public class StationRegionResolver {

  private final EntityCache<Region> regionCache;

  public StationRegionResolver(EntityCache<Region> regionCache) {
    this.regionCache = regionCache;
  }

  @SchemaMapping(typeName = "Station", field = "region")
  public Region resolve(Station station) {
    if (station.getRegionId() == null) {
      return null;
    }
    return regionCache.get(station.getRegionId());
  }
}
