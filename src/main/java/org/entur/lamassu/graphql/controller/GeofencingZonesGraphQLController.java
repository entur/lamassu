package org.entur.lamassu.graphql.controller;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.entur.lamassu.cache.GeofencingZonesCache;
import org.entur.lamassu.graphql.validation.GraphQLQueryValidationService;
import org.entur.lamassu.model.entities.GeofencingZones;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class GeofencingZonesGraphQLController extends BaseGraphQLController {

  private final GeofencingZonesCache geofencingZonesCache;
  private final GraphQLQueryValidationService validationService;

  public GeofencingZonesGraphQLController(
    GeofencingZonesCache geofencingZonesCache,
    GraphQLQueryValidationService validationService
  ) {
    this.geofencingZonesCache = geofencingZonesCache;
    this.validationService = validationService;
  }

  @QueryMapping
  public Collection<GeofencingZones> geofencingZones(@Argument List<String> systemIds) {
    validationService.validateSystems(systemIds);
    if (systemIds != null && !systemIds.isEmpty()) {
      return geofencingZonesCache.getAll(Set.copyOf(systemIds));
    }
    return geofencingZonesCache.getAll();
  }
}
