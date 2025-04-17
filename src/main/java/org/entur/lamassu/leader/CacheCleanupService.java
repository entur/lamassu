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

package org.entur.lamassu.leader;

import org.entur.lamassu.cache.EntityCache;
import org.entur.lamassu.cache.GBFSV2FeedCache;
import org.entur.lamassu.cache.GBFSV3FeedCache;
import org.entur.lamassu.leader.entityupdater.StationsUpdater;
import org.entur.lamassu.leader.entityupdater.VehiclesUpdater;
import org.entur.lamassu.model.entities.System;
import org.entur.lamassu.model.provider.FeedProvider;
import org.entur.lamassu.service.FeedProviderService;
import org.mobilitydata.gbfs.v2_3.gbfs.GBFSFeedName;
import org.mobilitydata.gbfs.v3_0.gbfs.GBFSFeed;
import org.springframework.stereotype.Service;

/**
 * Service responsible for cleaning up caches for removed or disabled systems.
 * This includes both entity caches and feed caches.
 */
@Service
public class CacheCleanupService {

  private final EntityCache<System> systemCache;
  private final FeedProviderService feedProviderService;
  private final VehiclesUpdater vehiclesUpdater;
  private final StationsUpdater stationsUpdater;
  private final GbfsUpdateContinuityTracker gbfsUpdateContinuityTracker;
  private final GBFSV2FeedCache gbfsV2FeedCache;
  private final GBFSV3FeedCache gbfsV3FeedCache;

  public CacheCleanupService(
    EntityCache<System> systemCache,
    FeedProviderService feedProviderService,
    VehiclesUpdater vehiclesUpdater,
    StationsUpdater stationsUpdater,
    GbfsUpdateContinuityTracker gbfsUpdateContinuityTracker,
    GBFSV2FeedCache gbfsV2FeedCache,
    GBFSV3FeedCache gbfsV3FeedCache
  ) {
    this.systemCache = systemCache;
    this.feedProviderService = feedProviderService;
    this.vehiclesUpdater = vehiclesUpdater;
    this.stationsUpdater = stationsUpdater;
    this.gbfsUpdateContinuityTracker = gbfsUpdateContinuityTracker;
    this.gbfsV2FeedCache = gbfsV2FeedCache;
    this.gbfsV3FeedCache = gbfsV3FeedCache;
  }

  /**
   * Cleans up all caches for systems that no longer have feed providers.
   * This should be called on startup to ensure clean state.
   */
  public void clearCache() {
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
        cleanupSystemCaches(feedProvider);
      });
  }

  /**
   * Cleans up all caches for a specific system.
   * This includes entity caches and feed caches.
   *
   * @param systemId The ID of the system to clean up
   */
  public void clearCacheForSystem(String systemId) {
    var feedProvider = feedProviderService.getFeedProviderBySystemId(systemId);
    if (feedProvider != null) {
      cleanupSystemCaches(feedProvider);
    }
  }

  /**
   * Cleans up all caches for a specific feed provider.
   * This includes entity caches and feed caches.
   *
   * @param feedProvider The feed provider to clean up caches for
   */
  public void cleanupSystemCaches(FeedProvider feedProvider) {
    String systemId = feedProvider.getSystemId();

    // Clean up entity caches
    vehiclesUpdater.clearExistingEntities(feedProvider);
    stationsUpdater.clearExistingEntities(feedProvider);

    // Clean up continuity tracking
    gbfsUpdateContinuityTracker.clearStationUpdateContinuity(systemId);
    gbfsUpdateContinuityTracker.clearVehicleUpdateContinuity(systemId);

    // Clean up V2 feed caches
    cleanupV2FeedCaches(feedProvider);

    // Clean up V3 feed caches
    cleanupV3FeedCaches(feedProvider);
  }

  /**
   * Cleans up all V2 feed caches for a specific feed provider.
   *
   * @param feedProvider The feed provider to clean up V2 feed caches for
   */
  private void cleanupV2FeedCaches(FeedProvider feedProvider) {
    // Clean up each feed type in the V2 cache
    for (GBFSFeedName feedName : GBFSFeedName.values()) {
      gbfsV2FeedCache.remove(feedName, feedProvider);
    }
  }

  /**
   * Cleans up all V3 feed caches for a specific feed provider.
   *
   * @param feedProvider The feed provider to clean up V3 feed caches for
   */
  private void cleanupV3FeedCaches(FeedProvider feedProvider) {
    // Clean up each feed type in the V3 cache
    for (GBFSFeed.Name feedName : GBFSFeed.Name.values()) {
      gbfsV3FeedCache.remove(feedName, feedProvider);
    }
  }
}
