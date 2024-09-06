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
import org.entur.lamassu.service.BoundingBoxQueryParameters;
import org.entur.lamassu.service.FeedProviderService;
import org.entur.lamassu.service.GeoSearchService;
import org.entur.lamassu.service.RangeQueryParameters;
import org.entur.lamassu.service.StationFilterParameters;
import org.entur.lamassu.service.VehicleFilterParameters;
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

  public Collection<Operator> getOperators() {
    return feedProviderService.getOperators();
  }

  public Collection<Vehicle> getVehicles(
    Set<String> ids,
    Double lat,
    Double lon,
    Double range,
    Double minimumLatitude,
    Double minimumLongitude,
    Double maximumLatitude,
    Double maximumLongitude,
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

    validateCount(count);
    validateCodespaces(codespaces);
    validateSystems(systems);

    var filterParams = new VehicleFilterParameters();
    filterParams.setCodespaces(codespaces);
    filterParams.setSystems(systems);
    filterParams.setOperators(operators);
    filterParams.setFormFactors(formFactors);
    filterParams.setPropulsionTypes(propulsionTypes);
    filterParams.setIncludeReserved(includeReserved);
    filterParams.setIncludeDisabled(includeDisabled);
    filterParams.setCount(count);

    if (
      isBoundingBoxSearch(
        minimumLatitude,
        minimumLongitude,
        maximumLatitude,
        maximumLongitude
      )
    ) {
      var boundingBoxQueryParameters = new BoundingBoxQueryParameters();
      boundingBoxQueryParameters.setMinimumLatitude(minimumLatitude);
      boundingBoxQueryParameters.setMinimumLongitude(minimumLongitude);
      boundingBoxQueryParameters.setMaximumLatitude(maximumLatitude);
      boundingBoxQueryParameters.setMaximumLongitude(maximumLongitude);

      logger.debug(
        "getVehicles called boundingBoxQueryParameters={} filter={}",
        boundingBoxQueryParameters,
        filterParams
      );

      return geoSearchService.getVehiclesInBoundingBox(
        boundingBoxQueryParameters,
        filterParams
      );
    } else if (isRangeSearch(range, lat, lon)) {
      validateRange(range);

      var rangeQueryParameters = new RangeQueryParameters();
      rangeQueryParameters.setLat(lat);
      rangeQueryParameters.setLon(lon);
      rangeQueryParameters.setRange(range);

      logger.debug(
        "getVehicles called rangeQueryParameters={} filter={}",
        rangeQueryParameters,
        filterParams
      );

      return geoSearchService.getVehiclesWithinRange(rangeQueryParameters, filterParams);
    } else {
      throw new GraphqlErrorException.Builder()
        .message(
          "You must either specify lat, lon and range OR minimumLatitude, minimumLongitude, maximumLatitude and maximumLongitude"
        )
        .build();
    }
  }

  public Vehicle getVehicle(String id) {
    return vehicleCache.get(id);
  }

  public Collection<Station> getStations(
    Set<String> ids,
    Double lat,
    Double lon,
    Double range,
    Double minimumLatitude,
    Double minimumLongitude,
    Double maximumLatitude,
    Double maximumLongitude,
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

    validateCount(count);
    validateCodespaces(codespaces);
    validateSystems(systems);

    var filterParams = new StationFilterParameters();
    filterParams.setCodespaces(codespaces);
    filterParams.setSystems(systems);
    filterParams.setOperators(operators);
    filterParams.setAvailableFormFactors(availableFormFactors);
    filterParams.setAvailablePropulsionTypes(availablePropulsionTypes);
    filterParams.setCount(count);

    if (
      isBoundingBoxSearch(
        minimumLatitude,
        minimumLongitude,
        maximumLatitude,
        maximumLongitude
      )
    ) {
      var boundingBoxQueryParameters = new BoundingBoxQueryParameters();
      boundingBoxQueryParameters.setMinimumLatitude(minimumLatitude);
      boundingBoxQueryParameters.setMinimumLongitude(minimumLongitude);
      boundingBoxQueryParameters.setMaximumLatitude(maximumLatitude);
      boundingBoxQueryParameters.setMaximumLongitude(maximumLongitude);
      logger.debug(
        "getStations called boundingBoxQueryParameters={} filter={}",
        boundingBoxQueryParameters,
        filterParams
      );
      return geoSearchService.getStationsInBoundingBox(
        boundingBoxQueryParameters,
        filterParams
      );
    } else if (isRangeSearch(range, lat, lon)) {
      validateRange(range);
      var rangeQueryParameters = new RangeQueryParameters();
      rangeQueryParameters.setLat(lat);
      rangeQueryParameters.setLon(lon);
      rangeQueryParameters.setRange(range);
      logger.debug(
        "getStations called rangeQueryParameters={} filter={}",
        rangeQueryParameters,
        filterParams
      );
      return geoSearchService.getStationsWithinRange(rangeQueryParameters, filterParams);
    } else {
      throw new GraphqlErrorException.Builder()
        .message(
          "You must either specify lat, lon and range OR minimumLatitude, minimumLongitude, maximumLatitude and maximumLongitude"
        )
        .build();
    }
  }

  private boolean isRangeSearch(Double range, Double lat, Double lon) {
    return range != null && lat != null && lon != null;
  }

  private boolean isBoundingBoxSearch(
    Double minimumLatitude,
    Double minimumLongitude,
    Double maximumLatitude,
    Double maximumLongitude
  ) {
    return (
      minimumLatitude != null &&
      minimumLongitude != null &&
      maximumLatitude != null &&
      maximumLongitude != null
    );
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
