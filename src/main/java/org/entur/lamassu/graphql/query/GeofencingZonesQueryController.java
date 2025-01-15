package org.entur.lamassu.graphql.query;

import java.util.List;
import java.util.Set;
import org.entur.lamassu.cache.EntityReader;
import org.entur.lamassu.graphql.validation.QueryParameterValidator;
import org.entur.lamassu.model.entities.GeofencingZones;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class GeofencingZonesQueryController {

  private final EntityReader<GeofencingZones> geofencingZonesReader;
  private final QueryParameterValidator validationService;

  public GeofencingZonesQueryController(
    EntityReader<GeofencingZones> geofencingZonesReader,
    QueryParameterValidator validationService
  ) {
    this.geofencingZonesReader = geofencingZonesReader;
    this.validationService = validationService;
  }

  @QueryMapping
  public List<GeofencingZones> geofencingZones(@Argument List<String> systemIds) {
    validationService.validateSystems(systemIds);
    if (systemIds != null && !systemIds.isEmpty()) {
      return geofencingZonesReader.getAll(Set.copyOf(systemIds));
    }
    return geofencingZonesReader.getAll();
  }
}
