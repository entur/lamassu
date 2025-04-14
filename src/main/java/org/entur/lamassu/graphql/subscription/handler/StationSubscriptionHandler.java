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
import org.entur.lamassu.graphql.subscription.filter.StationUpdateFilter;
import org.entur.lamassu.graphql.subscription.model.StationUpdate;
import org.entur.lamassu.graphql.subscription.model.UpdateType;
import org.entur.lamassu.model.entities.Station;
import org.entur.lamassu.service.BoundingBoxQueryParameters;
import org.entur.lamassu.service.GeoSearchService;
import org.entur.lamassu.service.RangeQueryParameters;
import org.entur.lamassu.service.StationFilterParameters;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Subscription handler for station updates.
 * Filters station updates based on subscription parameters.
 */
@Component
public class StationSubscriptionHandler
  extends EntitySubscriptionHandler<Station, StationUpdate> {

  private static Logger logger = LoggerFactory.getLogger(
    StationSubscriptionHandler.class
  );

  private final GeoSearchService geoSearchService;

  /**
   * Creates a new StationSubscriptionHandler with bounding box parameters.
   *
   * @param geoSearchService The geo search service
   */
  public StationSubscriptionHandler(
    GeoSearchService geoSearchService,
    EntityCache<Station> stationCache
  ) {
    super(stationCache);
    this.geoSearchService = geoSearchService;
  }

  @Override
  protected StationUpdate createUpdate(
    String id,
    Station station,
    UpdateType updateType
  ) {
    return new StationUpdate(id, updateType, station);
  }

  public Publisher<List<StationUpdate>> getPublisher(StationUpdateFilter filter) {
    var initialUpdates = getInitialUpdates(filter);
    return super.getPublisher(initialUpdates, filter);
  }

  private List<StationUpdate> getInitialUpdates(StationUpdateFilter filter) {
    List<Station> initialStations;

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
    List<StationUpdate> initialUpdates = initialStations
      .stream()
      .map(station -> createUpdate(station.getId(), station, UpdateType.CREATE))
      .toList();

    logger.trace("Mapped to {} matching stations", initialUpdates.size());

    return initialUpdates;
  }

  private List<Station> getInitialUpdates(
    StationFilterParameters filterParams,
    RangeQueryParameters queryParams
  ) {
    return geoSearchService.getStationsWithinRange(queryParams, filterParams);
  }

  private List<Station> getInitialUpdates(
    StationFilterParameters filterParams,
    BoundingBoxQueryParameters queryParams
  ) {
    return geoSearchService.getStationsInBoundingBox(queryParams, filterParams);
  }
}
