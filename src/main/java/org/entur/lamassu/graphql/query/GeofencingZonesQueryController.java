package org.entur.lamassu.graphql.query;

import java.util.List;
import java.util.Set;
import org.entur.lamassu.cache.EntityReader;
import org.entur.lamassu.graphql.validation.QueryParameterValidator;
import org.entur.lamassu.model.entities.GeofencingZones;
import org.entur.lamassu.model.entities.GeofencingZonesData;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class GeofencingZonesQueryController {

  private final EntityReader<GeofencingZones> geofencingZonesReader;
  private final EntityReader<GeofencingZonesData> geofencingZonesDataReader;
  private final QueryParameterValidator validationService;

  public GeofencingZonesQueryController(
    EntityReader<GeofencingZones> geofencingZonesReader,
    EntityReader<GeofencingZonesData> geofencingZonesDataReader,
    QueryParameterValidator validationService
  ) {
    this.geofencingZonesReader = geofencingZonesReader;
    this.geofencingZonesDataReader = geofencingZonesDataReader;
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

  @QueryMapping
  public List<GeofencingZonesData> geofencingZonesData(@Argument List<String> systemIds) {
    validationService.validateSystems(systemIds);
    if (systemIds != null && !systemIds.isEmpty()) {
      return geofencingZonesDataReader.getAll(Set.copyOf(systemIds));
    }
    return geofencingZonesDataReader.getAll();
  }
}
