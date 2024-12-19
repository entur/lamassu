package org.entur.lamassu.graphql.controller;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.entur.lamassu.cache.GeofencingZonesCache;
import org.entur.lamassu.model.entities.GeofencingZones;
import org.entur.lamassu.service.FeedProviderService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class GeofencingZonesGraphQLController extends BaseGraphQLController {

  private final GeofencingZonesCache geofencingZonesCache;

  public GeofencingZonesGraphQLController(
    FeedProviderService feedProviderService,
    GeofencingZonesCache geofencingZonesCache
  ) {
    super(feedProviderService);
    this.geofencingZonesCache = geofencingZonesCache;
  }

  @QueryMapping
  public Collection<GeofencingZones> geofencingZones(@Argument List<String> systemIds) {
    validateSystems(systemIds);
    if (systemIds != null && !systemIds.isEmpty()) {
      return geofencingZonesCache.getAll(Set.copyOf(systemIds));
    }
    return geofencingZonesCache.getAll();
  }
}
