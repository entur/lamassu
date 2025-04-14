/*
 *
 *
 *  * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 *  * the European Commission - subsequent versions of the EUPL (the "Licence");
 *  * You may not use this work except in compliance with the Licence.
 *  * You may obtain a copy of the Licence at:
 *  *
 *  *   https://joinup.ec.europa.eu/software/page/eupl
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the Licence is distributed on an "AS IS" basis,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the Licence for the specific language governing permissions and
 *  * limitations under the Licence.
 *
 */

package org.entur.lamassu.graphql.subscription.handler;

import java.util.List;
import org.entur.lamassu.cache.EntityCache;
import org.entur.lamassu.graphql.subscription.filter.VehicleUpdateFilter;
import org.entur.lamassu.graphql.subscription.model.UpdateType;
import org.entur.lamassu.graphql.subscription.model.VehicleUpdate;
import org.entur.lamassu.model.entities.Vehicle;
import org.entur.lamassu.service.BoundingBoxQueryParameters;
import org.entur.lamassu.service.GeoSearchService;
import org.entur.lamassu.service.RangeQueryParameters;
import org.entur.lamassu.service.VehicleFilterParameters;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Subscription handler for vehicle updates.
 * Filters vehicle updates based on subscription parameters.
 */
@Component
public class VehicleSubscriptionHandler
  extends EntitySubscriptionHandler<Vehicle, VehicleUpdate> {

  private static Logger logger = LoggerFactory.getLogger(
    VehicleSubscriptionHandler.class
  );

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
    logger.trace("Preparing to send {} initial updates", initialUpdates.size());
    return super.getPublisher(initialUpdates, filter);
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

    logger.trace("Found {} stations for subscription", initialStations.size());

    // Convert to list of updates
    List<VehicleUpdate> initialUpdates = initialStations
      .stream()
      .map(vehicle -> createUpdate(vehicle.getId(), vehicle, UpdateType.CREATE))
      .toList();

    logger.trace("Mapped to {} matching stations", initialUpdates.size());

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
