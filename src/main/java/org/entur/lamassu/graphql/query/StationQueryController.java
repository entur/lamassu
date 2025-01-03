package org.entur.lamassu.graphql.query;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.entur.lamassu.cache.StationCache;
import org.entur.lamassu.graphql.validation.QueryParameterValidator;
import org.entur.lamassu.model.entities.FormFactor;
import org.entur.lamassu.model.entities.PropulsionType;
import org.entur.lamassu.model.entities.Station;
import org.entur.lamassu.service.BoundingBoxQueryParameters;
import org.entur.lamassu.service.GeoSearchService;
import org.entur.lamassu.service.RangeQueryParameters;
import org.entur.lamassu.service.StationFilterParameters;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class StationQueryController {

  private final GeoSearchService geoSearchService;
  private final StationCache stationCache;
  private final QueryParameterValidator validationService;

  public StationQueryController(
    GeoSearchService geoSearchService,
    StationCache stationCache,
    QueryParameterValidator validationService
  ) {
    this.geoSearchService = geoSearchService;
    this.stationCache = stationCache;
    this.validationService = validationService;
  }

  @QueryMapping
  public Station station(@Argument String id) {
    return stationCache.get(id);
  }

  @QueryMapping
  public Collection<Station> stations(
    @Argument List<String> ids,
    @Argument Double lat,
    @Argument Double lon,
    @Argument Double range,
    @Argument Double minimumLatitude,
    @Argument Double minimumLongitude,
    @Argument Double maximumLatitude,
    @Argument Double maximumLongitude,
    @Argument Integer count,
    @Argument List<String> codespaces,
    @Argument List<String> systems,
    @Argument List<String> operators,
    @Argument List<FormFactor> availableFormFactors,
    @Argument List<PropulsionType> availablePropulsionTypes
  ) {
    if (ids != null && !ids.isEmpty()) {
      return stationCache.getAll(Set.copyOf(ids));
    }

    validationService.validateCount(count);
    validationService.validateCodespaces(codespaces);
    validationService.validateSystems(systems);

    var filterParams = new StationFilterParameters(
      codespaces,
      systems,
      operators,
      count,
      availableFormFactors,
      availablePropulsionTypes
    );

    Collection<Station> stations;

    validationService.validateQueryParameters(
      lat,
      lon,
      range,
      minimumLatitude,
      minimumLongitude,
      maximumLatitude,
      maximumLongitude
    );

    if (validationService.isRangeSearch(range, lat, lon)) {
      var queryParams = new RangeQueryParameters(lat, lon, range);
      stations = geoSearchService.getStationsWithinRange(queryParams, filterParams);
    } else {
      var queryParams = new BoundingBoxQueryParameters(
        minimumLatitude,
        minimumLongitude,
        maximumLatitude,
        maximumLongitude
      );
      stations = geoSearchService.getStationsInBoundingBox(queryParams, filterParams);
    }

    if (count != null) {
      return stations.stream().limit(count).toList();
    }

    return stations;
  }

  @QueryMapping
  public Collection<Station> stationsById(@Argument List<String> ids) {
    return stationCache.getAll(Set.copyOf(ids));
  }
}
