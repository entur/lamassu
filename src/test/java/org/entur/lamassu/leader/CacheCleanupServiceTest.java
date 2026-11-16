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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.entur.lamassu.cache.EntityCache;
import org.entur.lamassu.cache.GBFSV2FeedCache;
import org.entur.lamassu.cache.GBFSV3FeedCache;
import org.entur.lamassu.leader.entityupdater.StationsUpdater;
import org.entur.lamassu.leader.entityupdater.VehiclesUpdater;
import org.entur.lamassu.model.entities.System;
import org.entur.lamassu.service.FeedProviderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CacheCleanupServiceTest {

  @Mock
  private EntityCache<System> systemCache;

  @Mock
  private FeedProviderService feedProviderService;

  @Mock
  private VehiclesUpdater vehiclesUpdater;

  @Mock
  private StationsUpdater stationsUpdater;

  @Mock
  private GbfsUpdateContinuityTracker continuityTracker;

  @Mock
  private GBFSV2FeedCache gbfsV2FeedCache;

  @Mock
  private GBFSV3FeedCache gbfsV3FeedCache;

  @Mock
  private OrphanedEntityCleaner orphanedEntityCleaner;

  private CacheCleanupService service() {
    return new CacheCleanupService(
      systemCache,
      feedProviderService,
      vehiclesUpdater,
      stationsUpdater,
      continuityTracker,
      gbfsV2FeedCache,
      gbfsV3FeedCache,
      orphanedEntityCleaner
    );
  }

  @Test
  void clearCacheRemovesOrphanedEntitiesEvenWhenTheSystemCacheIsEmpty() {
    // The system entries of a disabled provider expire after a day, so the system cache
    // can know nothing about the systems whose entities were left behind.
    when(systemCache.getAll()).thenReturn(List.of());

    service().clearCache();

    verify(orphanedEntityCleaner).removeOrphans();
  }

  @Test
  void clearCacheStillCompletesWhenRemovingOrphanedEntitiesFails() {
    // clearCache runs before the subscriptions are created, so an exception escaping here
    // would leave the leader running without polling anything.
    when(orphanedEntityCleaner.removeOrphans())
      .thenThrow(new IllegalStateException("redis timeout"));
    when(systemCache.getAll()).thenReturn(List.of());

    assertDoesNotThrow(() -> service().clearCache());

    verify(systemCache).getAll();
  }
}
