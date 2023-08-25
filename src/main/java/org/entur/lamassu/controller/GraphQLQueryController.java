package org.entur.lamassu.controller;

import graphql.GraphqlErrorException;
import graphql.kickstart.tools.GraphQLQueryResolver;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.entur.lamassu.cache.GeofencingZonesCache;
import org.entur.lamassu.cache.StationCache;
import org.entur.lamassu.cache.VehicleCache;
import org.entur.lamassu.model.entities.FormFactor;
import org.entur.lamassu.model.entities.GeofencingZones;
import org.entur.lamassu.model.entities.Operator;
import org.entur.lamassu.model.entities.PropulsionType;
import org.entur.lamassu.model.entities.Station;
import org.entur.lamassu.model.entities.Vehicle;
import org.entur.lamassu.model.provider.FeedProvider;
import org.entur.lamassu.service.FeedProviderService;
import org.entur.lamassu.service.GeoSearchService;
import org.entur.lamassu.service.RangeQueryParameters;
import org.entur.lamassu.service.StationFilterParameters;
import org.entur.lamassu.service.VehicleFilterParameters;
import org.entur.lamassu.util.OperatorFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GraphQLQueryController implements GraphQLQueryResolver {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  private final GeoSearchService geoSearchService;
  private final FeedProviderService feedProviderService;
  private final VehicleCache vehicleCache;
  private final StationCache stationCache;
  private final GeofencingZonesCache geofencingZonesCache;

  @Autowired
  public GraphQLQueryController(
    GeoSearchService geoSearchService,
    FeedProviderService feedProviderService,
    VehicleCache vehicleCache,
    StationCache stationCache,
    GeofencingZonesCache geofencingZonesCache
  ) {
    this.geoSearchService = geoSearchService;
    this.feedProviderService = feedProviderService;
    this.vehicleCache = vehicleCache;
    this.stationCache = stationCache;
    this.geofencingZonesCache = geofencingZonesCache;
  }

  public Collection<String> getCodespaces() {
    return feedProviderService
      .getFeedProviders()
      .stream()
      .map(FeedProvider::getCodespace)
      .collect(Collectors.toSet());
  }

  public Collection<Operator> getOperators(List<FormFactor> formFactors) {
    return feedProviderService.getOperators(new OperatorFilter(formFactors));
  }

  public Collection<Vehicle> getVehicles(
    Set<String> ids,
    Double lat,
    Double lon,
    Double range,
    Integer count,
    List<String> codespaces,
    List<String> systems,
    List<String> operators,
    List<FormFactor> formFactors,
    List<PropulsionType> propulsionTypes,
    boolean includeReserved,
    boolean includeDisabled
  ) {
    if (ids != null) {
      return vehicleCache.getAll(ids);
    }

    validateRange(range);
    validateCount(count);
    validateCodespaces(codespaces);
    validateSystems(systems);

    var queryParams = new RangeQueryParameters();
    queryParams.setLat(lat);
    queryParams.setLon(lon);
    queryParams.setRange(range);
    queryParams.setCount(count);

    var filterParams = new VehicleFilterParameters();
    filterParams.setCodespaces(codespaces);
    filterParams.setSystems(systems);
    filterParams.setOperators(operators);
    filterParams.setFormFactors(formFactors);
    filterParams.setPropulsionTypes(propulsionTypes);
    filterParams.setIncludeReserved(includeReserved);
    filterParams.setIncludeDisabled(includeDisabled);

    logger.debug("getVehicles called query={} filter={}", queryParams, filterParams);

    return geoSearchService.getVehiclesNearby(queryParams, filterParams);
  }

  public Vehicle getVehicle(String id) {
    return vehicleCache.get(id);
  }

  public Collection<Station> getStations(
    Set<String> ids,
    Double lat,
    Double lon,
    Double range,
    Integer count,
    List<String> codespaces,
    List<String> systems,
    List<String> operators,
    List<FormFactor> availableFormFactors,
    List<PropulsionType> availablePropulsionTypes
  ) {
    if (ids != null) {
      return stationCache.getAll(ids);
    }

    validateRange(range);
    validateCount(count);
    validateCodespaces(codespaces);
    validateSystems(systems);

    var queryParams = new RangeQueryParameters();
    queryParams.setLat(lat);
    queryParams.setLon(lon);
    queryParams.setRange(range);
    queryParams.setCount(count);

    var filterParams = new StationFilterParameters();
    filterParams.setCodespaces(codespaces);
    filterParams.setSystems(systems);
    filterParams.setOperators(operators);
    filterParams.setAvailableFormFactors(availableFormFactors);
    filterParams.setAvailablePropulsionTypes(availablePropulsionTypes);

    logger.debug("getStations called query={} filter={}", queryParams, filterParams);

    return geoSearchService.getStationsNearby(queryParams, filterParams);
  }

  public Station getStation(String id) {
    return stationCache.get(id);
  }

  public Collection<Station> getStationsById(List<String> ids) {
    logger.debug("getStationsByIds called ids={}", ids);
    return stationCache.getAll(new HashSet<>(ids));
  }

  public Collection<GeofencingZones> geofencingZones(List<String> systemIds) {
    logger.debug("geofencingZones called systemIds={}", systemIds);

    validateSystems(systemIds);

    if (systemIds == null || systemIds.isEmpty()) {
      return geofencingZonesCache.getAll();
    } else {
      return geofencingZonesCache.getAll(new HashSet<>(systemIds));
    }
  }

  private void validateCount(Integer count) {
    if (count != null) {
      validate(p -> p > 0, count, "Count must be positive");
    }
  }

  private void validateRange(Double range) {
    validate(p -> p > -1, range, "Range must be non-negative");
  }

  private void validateCodespaces(List<String> codespaces) {
    if (codespaces != null) {
      var validCodespaces = getCodespaces();
      validate(validCodespaces::containsAll, codespaces, "Unknown codespace(s)");
    }
  }

  private void validateSystems(List<String> systems) {
    if (systems != null) {
      var validSystems = getSystems();
      validate(validSystems::containsAll, systems, "Unknown system(s)");
    }
  }

  private Collection<String> getSystems() {
    return feedProviderService
      .getFeedProviders()
      .stream()
      .map(FeedProvider::getSystemId)
      .collect(Collectors.toSet());
  }

  private <T> void validate(Predicate<T> predicate, T value, String message) {
    if (predicate.negate().test(value)) {
      throw new GraphqlErrorException.Builder().message(message).build();
    }
  }
}
