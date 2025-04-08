package org.entur.lamassu.graphql.subscription;

import java.util.Collection;
import java.util.List;
import org.entur.lamassu.model.entities.Station;
import org.entur.lamassu.model.subscription.StationUpdate;
import org.entur.lamassu.model.subscription.UpdateType;
import org.entur.lamassu.service.BoundingBoxQueryParameters;
import org.entur.lamassu.service.GeoSearchService;
import org.entur.lamassu.service.RangeQueryParameters;
import org.entur.lamassu.service.StationFilterParameters;

/**
 * Subscription handler for station updates.
 * Filters station updates based on subscription parameters.
 */
public class StationSubscriptionHandler
  extends EntitySubscriptionHandler<Station, StationUpdate> {

  private final StationFilterParameters filterParams;
  private final GeoSearchService geoSearchService;
  private final BoundingBoxQueryParameters boundingBoxParams;
  private final RangeQueryParameters rangeParams;

  /**
   * Creates a new StationSubscriptionHandler with bounding box parameters.
   *
   * @param filterParams The filter parameters
   * @param geoSearchService The geo search service
   * @param boundingBoxParams The bounding box parameters
   */
  public StationSubscriptionHandler(
    StationFilterParameters filterParams,
    GeoSearchService geoSearchService,
    BoundingBoxQueryParameters boundingBoxParams
  ) {
    super();
    this.filterParams = filterParams;
    this.geoSearchService = geoSearchService;
    this.boundingBoxParams = boundingBoxParams;
    this.rangeParams = null;
  }

  /**
   * Creates a new StationSubscriptionHandler with range parameters.
   *
   * @param filterParams The filter parameters
   * @param geoSearchService The geo search service
   * @param rangeParams The range parameters
   */
  public StationSubscriptionHandler(
    StationFilterParameters filterParams,
    GeoSearchService geoSearchService,
    RangeQueryParameters rangeParams
  ) {
    super();
    this.filterParams = filterParams;
    this.geoSearchService = geoSearchService;
    this.boundingBoxParams = null;
    this.rangeParams = rangeParams;
  }

  @Override
  protected boolean matchesFilter(Station station) {
    // Check if station matches filter parameters
    if (
      filterParams.getCodespaces() != null &&
      !filterParams.getCodespaces().isEmpty() &&
      !filterParams.getCodespaces().contains(station.getSystem().getId().split(":")[0])
    ) {
      return false;
    }

    if (
      filterParams.getSystems() != null &&
      !filterParams.getSystems().isEmpty() &&
      !filterParams.getSystems().contains(station.getSystem().getId())
    ) {
      return false;
    }

    if (
      filterParams.getOperators() != null &&
      !filterParams.getOperators().isEmpty() &&
      !filterParams.getOperators().contains(station.getSystem().getOperator().getId())
    ) {
      return false;
    }

    if (
      filterParams.getAvailableFormFactors() != null &&
      !filterParams.getAvailableFormFactors().isEmpty()
    ) {
      boolean hasMatchingFormFactor = false;
      if (station.getVehicleTypesAvailable() != null) {
        for (var availability : station.getVehicleTypesAvailable()) {
          if (
            filterParams
              .getAvailableFormFactors()
              .contains(availability.getVehicleType().getFormFactor())
          ) {
            hasMatchingFormFactor = true;
            break;
          }
        }
      }
      if (!hasMatchingFormFactor) {
        return false;
      }
    }

    if (
      filterParams.getAvailablePropulsionTypes() != null &&
      !filterParams.getAvailablePropulsionTypes().isEmpty()
    ) {
      boolean hasMatchingPropulsionType = false;
      if (station.getVehicleTypesAvailable() != null) {
        for (var availability : station.getVehicleTypesAvailable()) {
          if (
            filterParams
              .getAvailablePropulsionTypes()
              .contains(availability.getVehicleType().getPropulsionType())
          ) {
            hasMatchingPropulsionType = true;
            break;
          }
        }
      }
      if (!hasMatchingPropulsionType) {
        return false;
      }
    }

    // Check if station is within geographic bounds
    if (rangeParams != null) {
      double distance = calculateDistance(
        rangeParams.getLat(),
        rangeParams.getLon(),
        station.getLat(),
        station.getLon()
      );
      return distance <= rangeParams.getRange();
    } else if (boundingBoxParams != null) {
      return (
        station.getLat() >= boundingBoxParams.getMinimumLatitude() &&
        station.getLat() <= boundingBoxParams.getMaximumLatitude() &&
        station.getLon() >= boundingBoxParams.getMinimumLongitude() &&
        station.getLon() <= boundingBoxParams.getMaximumLongitude()
      );
    }

    return true;
  }

  @Override
  protected StationUpdate createUpdate(
    String id,
    Station station,
    UpdateType updateType
  ) {
    return new StationUpdate(id, updateType, station);
  }

  @Override
  protected List<StationUpdate> getInitialUpdates() {
    // Get initial data matching the subscription
    Collection<Station> initialStations;
    if (rangeParams != null) {
      initialStations =
        geoSearchService.getStationsWithinRange(rangeParams, filterParams);
    } else {
      initialStations =
        geoSearchService.getStationsInBoundingBox(boundingBoxParams, filterParams);
    }

    System.out.println("Found " + initialStations.size() + " stations for subscription");

    // Convert to list of updates
    List<StationUpdate> initialUpdates = initialStations
      .stream()
      //.filter(this::matchesFilter)
      .map(station -> new StationUpdate(station.getId(), UpdateType.CREATE, station))
      .collect(java.util.stream.Collectors.toList());

    System.out.println("Filtered to " + initialUpdates.size() + " matching stations");

    return initialUpdates;
  }

  /**
   * Calculates the distance between two points using the Haversine formula.
   *
   * @param lat1 Latitude of point 1
   * @param lon1 Longitude of point 1
   * @param lat2 Latitude of point 2
   * @param lon2 Longitude of point 2
   * @return The distance in meters
   */
  private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
    final int R = 6371000; // Earth radius in meters

    double latDistance = Math.toRadians(lat2 - lat1);
    double lonDistance = Math.toRadians(lon2 - lon1);
    double a =
      Math.sin(latDistance / 2) *
      Math.sin(latDistance / 2) +
      Math.cos(Math.toRadians(lat1)) *
      Math.cos(Math.toRadians(lat2)) *
      Math.sin(lonDistance / 2) *
      Math.sin(lonDistance / 2);
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

    return R * c;
  }
}
