package org.entur.lamassu.graphql.subscription;

import java.util.List;
import org.entur.lamassu.cache.EntityCache;
import org.entur.lamassu.model.entities.Vehicle;
import org.entur.lamassu.model.subscription.UpdateType;
import org.entur.lamassu.model.subscription.VehicleUpdate;
import org.entur.lamassu.service.BoundingBoxQueryParameters;
import org.entur.lamassu.service.GeoSearchService;
import org.entur.lamassu.service.RangeQueryParameters;
import org.entur.lamassu.service.VehicleFilterParameters;
import org.reactivestreams.Publisher;

/**
 * Subscription handler for vehicle updates.
 * Filters vehicle updates based on subscription parameters.
 */
public class VehicleSubscriptionHandler
  extends EntitySubscriptionHandler<Vehicle, VehicleUpdate> {

  private final GeoSearchService geoSearchService;

  /**
   * Creates a new VehicleSubscriptionHandler with bounding box parameters.
   *
   * @param geoSearchService The geo search service
   */
  public VehicleSubscriptionHandler(
    GeoSearchService geoSearchService,
    EntityCache<Vehicle> vehicleCache
  ) {
    super(vehicleCache);
    this.geoSearchService = geoSearchService;
  }

  @Override
  protected VehicleUpdate createUpdate(
    String id,
    Vehicle vehicle,
    UpdateType updateType
  ) {
    return new VehicleUpdate(id, updateType, vehicle);
  }

  public Publisher<List<VehicleUpdate>> getPublisher(VehicleUpdateFilter filter) {
    List<VehicleUpdate> initialUpdates = getInitialUpdates(filter);
    System.out.println("Preparing to send " + initialUpdates.size() + " initial updates");
    return super.getPublisher(initialUpdates, filter::matches);
  }

  private List<VehicleUpdate> getInitialUpdates(VehicleUpdateFilter filter) {
    List<Vehicle> initialStations;

    if (filter.getBoundingBoxParameters() != null) {
      initialStations =
        getInitialUpdates(
          filter.getFilterParameters(),
          filter.getBoundingBoxParameters()
        );
    } else {
      initialStations =
        getInitialUpdates(filter.getFilterParameters(), filter.getRangeQueryParameters());
    }

    System.out.println("Found " + initialStations.size() + " stations for subscription");

    // Convert to list of updates
    List<VehicleUpdate> initialUpdates = initialStations
      .stream()
      .map(vehicle -> createUpdate(vehicle.getId(), vehicle, UpdateType.CREATE))
      .collect(java.util.stream.Collectors.toList());

    System.out.println("Mapped to " + initialUpdates.size() + " matching stations");

    return initialUpdates;
  }

  private List<Vehicle> getInitialUpdates(
    VehicleFilterParameters filterParams,
    RangeQueryParameters queryParams
  ) {
    return geoSearchService.getVehiclesWithinRange(queryParams, filterParams);
  }

  private List<Vehicle> getInitialUpdates(
    VehicleFilterParameters filterParams,
    BoundingBoxQueryParameters queryParams
  ) {
    return geoSearchService.getVehiclesInBoundingBox(queryParams, filterParams);
  }
}
