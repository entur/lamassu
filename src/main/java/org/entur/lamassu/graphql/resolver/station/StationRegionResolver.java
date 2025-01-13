package org.entur.lamassu.graphql.resolver.station;

import org.entur.lamassu.cache.EntityReader;
import org.entur.lamassu.model.entities.Region;
import org.entur.lamassu.model.entities.Station;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

@Controller
public class StationRegionResolver {

  private final EntityReader<Region> regionReader;

  public StationRegionResolver(EntityReader<Region> regionReader) {
    this.regionReader = regionReader;
  }

  @SchemaMapping(typeName = "Station", field = "region")
  public Region resolve(Station station) {
    if (station.getRegionId() == null) {
      return null;
    }
    return regionReader.get(station.getRegionId());
  }
}
