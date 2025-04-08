package org.entur.lamassu.graphql.subscription;

import java.util.List;
import org.entur.lamassu.cache.EntityCache;
import org.entur.lamassu.graphql.validation.QueryParameterValidator;
import org.entur.lamassu.model.entities.FormFactor;
import org.entur.lamassu.model.entities.PropulsionType;
import org.entur.lamassu.model.entities.Station;
import org.entur.lamassu.model.subscription.StationUpdate;
import org.entur.lamassu.service.BoundingBoxQueryParameters;
import org.entur.lamassu.service.GeoSearchService;
import org.entur.lamassu.service.RangeQueryParameters;
import org.entur.lamassu.service.StationFilterParameters;
import org.reactivestreams.Publisher;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.SubscriptionMapping;
import org.springframework.stereotype.Controller;

/**
 * Controller for station subscriptions.
 * Handles GraphQL subscription requests for station updates.
 */
@Controller
public class StationSubscriptionController {

  private final GeoSearchService geoSearchService;
  private final EntityCache<Station> stationCache;
  private final QueryParameterValidator validationService;

  /**
   * Creates a new StationSubscriptionController.
   *
   * @param geoSearchService The geo search service
   * @param stationCache The station cache
   * @param validationService The query parameter validator
   */
  public StationSubscriptionController(
    GeoSearchService geoSearchService,
    EntityCache<Station> stationCache,
    QueryParameterValidator validationService
  ) {
    this.geoSearchService = geoSearchService;
    this.stationCache = stationCache;
    this.validationService = validationService;
  }

  /**
   * Handles station subscription requests.
   * Creates a subscription that will receive updates for stations matching the specified criteria.
   *
   * @param lat Latitude for range search
   * @param lon Longitude for range search
   * @param range Range in meters for range search
   * @param minimumLatitude Minimum latitude for bounding box search
   * @param minimumLongitude Minimum longitude for bounding box search
   * @param maximumLatitude Maximum latitude for bounding box search
   * @param maximumLongitude Maximum longitude for bounding box search
   * @param codespaces List of codespaces to filter by
   * @param systems List of systems to filter by
   * @param operators List of operators to filter by
   * @param availableFormFactors List of available form factors to filter by
   * @param availablePropulsionTypes List of available propulsion types to filter by
   * @return A publisher that will emit station updates
   */
  @SubscriptionMapping
  public Publisher<List<StationUpdate>> stations(
    @Argument Double lat,
    @Argument Double lon,
    @Argument Integer range,
    @Argument Double minimumLatitude,
    @Argument Double minimumLongitude,
    @Argument Double maximumLatitude,
    @Argument Double maximumLongitude,
    @Argument List<String> codespaces,
    @Argument List<String> systems,
    @Argument List<String> operators,
    @Argument List<FormFactor> availableFormFactors,
    @Argument List<PropulsionType> availablePropulsionTypes
  ) {
    // Validate parameters
    validationService.validateCodespaces(codespaces);
    validationService.validateSystems(systems);
    validationService.validateQueryParameters(
      lat,
      lon,
      range != null ? (double) range : null,
      minimumLatitude,
      minimumLongitude,
      maximumLatitude,
      maximumLongitude
    );

    // Create filter parameters
    var filterParams = new StationFilterParameters(
      codespaces,
      systems,
      operators,
      null, // count is not applicable for subscriptions
      availableFormFactors,
      availablePropulsionTypes
    );

    // Create subscription handler
    StationSubscriptionHandler handler;
    if (
      validationService.isRangeSearch(range != null ? (double) range : null, lat, lon)
    ) {
      var queryParams = new RangeQueryParameters(
        lat,
        lon,
        range != null ? (double) range : null
      );
      handler =
        new StationSubscriptionHandler(filterParams, geoSearchService, queryParams);
    } else {
      var queryParams = new BoundingBoxQueryParameters(
        minimumLatitude,
        minimumLongitude,
        maximumLatitude,
        maximumLongitude
      );
      handler =
        new StationSubscriptionHandler(filterParams, geoSearchService, queryParams);
    }

    // Register handler as listener and store the ID for cleanup
    handler.setListenerId(stationCache.addListener(handler));

    // Return publisher and ensure listener is removed when subscription ends
    return handler.getPublisher();
  }
}
