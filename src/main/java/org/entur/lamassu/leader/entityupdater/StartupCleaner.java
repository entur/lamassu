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

package org.entur.lamassu.leader.entityupdater;

import org.entur.lamassu.cache.EntityCache;
import org.entur.lamassu.leader.GbfsUpdateContinuityTracker;
import org.entur.lamassu.model.entities.System;
import org.entur.lamassu.model.provider.FeedProvider;
import org.entur.lamassu.service.FeedProviderService;
import org.springframework.stereotype.Component;

/**
 * Helper service that cleans up entities for removed systems on startup
 */
@Component
public class StartupCleaner {

  private final EntityCache<System> systemCache;
  private final FeedProviderService feedProviderService;
  private final VehiclesUpdater vehiclesUpdater;
  private final StationsUpdater stationsUpdater;
  private final GbfsUpdateContinuityTracker gbfsUpdateContinuityTracker;

  public StartupCleaner(
    EntityCache<System> systemCache,
    FeedProviderService feedProviderService,
    VehiclesUpdater vehiclesUpdater,
    StationsUpdater stationsUpdater,
    GbfsUpdateContinuityTracker gbfsUpdateContinuityTracker
  ) {
    this.systemCache = systemCache;
    this.feedProviderService = feedProviderService;
    this.vehiclesUpdater = vehiclesUpdater;
    this.stationsUpdater = stationsUpdater;
    this.gbfsUpdateContinuityTracker = gbfsUpdateContinuityTracker;
  }

  public void cleanup() {
    systemCache
      .getAll()
      .stream()
      .filter(system ->
        feedProviderService.getFeedProviderBySystemId(system.getId()) == null
      )
      .forEach(system -> {
        var feedProvider = new FeedProvider();
        feedProvider.setSystemId(system.getId());
        feedProvider.setOperatorId(system.getOperator().getId());
        vehiclesUpdater.clearExistingEntities(feedProvider);
        stationsUpdater.clearExistingEntities(feedProvider);
        gbfsUpdateContinuityTracker.clearStationUpdateContinuity(system.getId());
        gbfsUpdateContinuityTracker.clearVehicleUpdateContinuity(system.getId());
      });
  }

  public void cleanupSystem(String systemId) {
    var feedProvider = feedProviderService.getFeedProviderBySystemId(systemId);
    if (feedProvider != null) {
      vehiclesUpdater.clearExistingEntities(feedProvider);
      stationsUpdater.clearExistingEntities(feedProvider);
      gbfsUpdateContinuityTracker.clearStationUpdateContinuity(systemId);
      gbfsUpdateContinuityTracker.clearVehicleUpdateContinuity(systemId);
    }
  }
}
