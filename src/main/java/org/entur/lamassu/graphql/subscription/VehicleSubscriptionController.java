package org.entur.lamassu.graphql.subscription;

import java.util.List;
import org.entur.lamassu.graphql.subscription.filter.VehicleUpdateFilter;
import org.entur.lamassu.graphql.subscription.handler.VehicleSubscriptionHandler;
import org.entur.lamassu.graphql.subscription.model.VehicleUpdate;
import org.entur.lamassu.graphql.validation.QueryParameterValidator;
import org.entur.lamassu.model.entities.FormFactor;
import org.entur.lamassu.model.entities.PropulsionType;
import org.entur.lamassu.service.BoundingBoxQueryParameters;
import org.entur.lamassu.service.FeedProviderService;
import org.entur.lamassu.service.RangeQueryParameters;
import org.entur.lamassu.service.VehicleFilterParameters;
import org.reactivestreams.Publisher;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.SubscriptionMapping;
import org.springframework.stereotype.Controller;

/**
 * Controller for vehicle subscriptions.
 * Handles GraphQL subscription requests for vehicle updates.
 */
@Controller
public class VehicleSubscriptionController {

  private final VehicleSubscriptionHandler vehicleSubscriptionHandler;
  private final QueryParameterValidator validationService;
  private final FeedProviderService feedProviderService;

  /**
   * Creates a new VehicleSubscriptionController.
   *
   * @param vehicleSubscriptionHandler
   * @param validationService The query parameter validator
   */
  public VehicleSubscriptionController(
    VehicleSubscriptionHandler vehicleSubscriptionHandler,
    QueryParameterValidator validationService,
    FeedProviderService feedProviderService
  ) {
    this.vehicleSubscriptionHandler = vehicleSubscriptionHandler;
    this.validationService = validationService;
    this.feedProviderService = feedProviderService;
  }

  /**
   * Handles vehicle subscription requests.
   * Creates a subscription that will receive updates for vehicles matching the specified criteria.
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
   * @param formFactors List of form factors to filter by
   * @param propulsionTypes List of propulsion types to filter by
   * @param includeReserved Whether to include reserved vehicles
   * @param includeDisabled Whether to include disabled vehicles
   * @return A publisher that will emit vehicle updates
   */
  @SubscriptionMapping
  public Publisher<List<VehicleUpdate>> vehicles(
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
    @Argument List<FormFactor> formFactors,
    @Argument List<PropulsionType> propulsionTypes,
    @Argument Boolean includeReserved,
    @Argument Boolean includeDisabled
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
    var filterParams = new VehicleFilterParameters(
      codespaces,
      systems,
      operators,
      null, // count is not applicable for subscriptions
      formFactors,
      propulsionTypes,
      includeReserved != null ? includeReserved : false,
      includeDisabled != null ? includeDisabled : false
    );

    // Create subscription handler
    VehicleUpdateFilter filter;
    if (
      validationService.isRangeSearch(range != null ? (double) range : null, lat, lon)
    ) {
      var queryParams = new RangeQueryParameters(
        lat,
        lon,
        range != null ? (double) range : null
      );
      filter =
        new VehicleUpdateFilter(
          filterParams,
          queryParams,
          systemId ->
            feedProviderService.getFeedProviderBySystemId(systemId).getCodespace()
        );
    } else {
      var queryParams = new BoundingBoxQueryParameters(
        minimumLatitude,
        minimumLongitude,
        maximumLatitude,
        maximumLongitude
      );
      filter =
        new VehicleUpdateFilter(
          filterParams,
          queryParams,
          systemId ->
            feedProviderService.getFeedProviderBySystemId(systemId).getCodespace()
        );
    }

    // Return publisher and ensure listener is removed when subscription ends
    return vehicleSubscriptionHandler.getPublisher(filter);
  }
}
