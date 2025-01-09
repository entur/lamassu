package org.entur.lamassu.graphql.query;

import java.util.List;
import java.util.Set;
import org.entur.lamassu.cache.EntityCache;
import org.entur.lamassu.graphql.validation.QueryParameterValidator;
import org.entur.lamassu.model.entities.GeofencingZones;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class GeofencingZonesQueryController {

  private final EntityCache<GeofencingZones> geofencingZonesCache;
  private final QueryParameterValidator validationService;

  public GeofencingZonesQueryController(
    EntityCache<GeofencingZones> geofencingZonesCache,
    QueryParameterValidator validationService
  ) {
    this.geofencingZonesCache = geofencingZonesCache;
    this.validationService = validationService;
  }

  @QueryMapping
  public List<GeofencingZones> geofencingZones(@Argument List<String> systemIds) {
    validationService.validateSystems(systemIds);
    if (systemIds != null && !systemIds.isEmpty()) {
      return geofencingZonesCache.getAll(Set.copyOf(systemIds));
    }
    return geofencingZonesCache.getAll();
  }
}
