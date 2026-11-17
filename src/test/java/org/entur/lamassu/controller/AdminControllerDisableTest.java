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

package org.entur.lamassu.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.entur.lamassu.cache.CacheManagementService;
import org.entur.lamassu.config.feedprovider.FeedProviderConfig;
import org.entur.lamassu.leader.FeedUpdater;
import org.entur.lamassu.leader.OrphanedEntityCleaner;
import org.entur.lamassu.model.entities.Vehicle;
import org.entur.lamassu.model.provider.FeedProvider;
import org.entur.lamassu.service.GeoSearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RMapCache;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class AdminControllerDisableTest {

  private static final String SYSTEM_ID = "test-system";

  @Mock
  private GeoSearchService geoSearchService;

  @Mock
  private RMapCache<String, Vehicle> vehicleCache;

  @Mock
  private FeedProviderConfig feedProviderConfig;

  @Mock
  private FeedUpdater feedUpdater;

  @Mock
  private CacheManagementService cacheManagementService;

  @Mock
  private OrphanedEntityCleaner orphanedEntityCleaner;

  private AdminController controller;
  private FeedProvider provider;

  @BeforeEach
  void setUp() {
    controller =
      new AdminController(
        geoSearchService,
        vehicleCache,
        feedProviderConfig,
        feedUpdater,
        cacheManagementService,
        orphanedEntityCleaner
      );
    provider = new FeedProvider();
    provider.setSystemId(SYSTEM_ID);
    provider.setEnabled(true);
  }

  @Test
  void bulkDisableReportsTheSystemsThatCouldNotBeCleanedUp() {
    var failing = new FeedProvider();
    failing.setSystemId("failing-system");
    failing.setEnabled(true);
    when(feedProviderConfig.getProviderBySystemId(SYSTEM_ID)).thenReturn(provider);
    when(feedProviderConfig.getProviderBySystemId("failing-system")).thenReturn(failing);
    when(feedUpdater.stopSubscription(provider)).thenReturn(true);
    when(feedUpdater.stopSubscription(failing)).thenReturn(false);

    var response = controller.bulkSetEnabled(
      new AdminController.BulkSetEnabledRequest(
        List.of(SYSTEM_ID, "failing-system"),
        false
      )
    );

    assertEquals("SUCCESS", response.getBody().get(SYSTEM_ID));
    assertEquals("FAILED", response.getBody().get("failing-system"));
    // Both are saved as disabled, so repeating the request retries the cleanup
    verify(feedProviderConfig).updateProvider(provider);
    verify(feedProviderConfig).updateProvider(failing);
  }

  @Test
  void bulkEnableDoesNotStopAnything() {
    when(feedProviderConfig.getProviderBySystemId(SYSTEM_ID)).thenReturn(provider);

    var response = controller.bulkSetEnabled(
      new AdminController.BulkSetEnabledRequest(List.of(SYSTEM_ID), true)
    );

    assertEquals("SUCCESS", response.getBody().get(SYSTEM_ID));
    verify(feedUpdater, never()).stopSubscription(any());
  }

  @Test
  void disablingOneProviderFailsWhenItCouldNotBeCleanedUp() {
    when(feedProviderConfig.getProviderBySystemId(SYSTEM_ID)).thenReturn(provider);
    when(feedUpdater.stopSubscription(provider)).thenReturn(false);

    var response = controller.setFeedProviderEnabled(SYSTEM_ID, false);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    verify(feedProviderConfig).updateProvider(provider);
  }

  @Test
  void savingAProviderAsDisabledStopsItAndRemovesItsData() {
    provider.setEnabled(false);
    when(feedProviderConfig.updateProvider(provider)).thenReturn(true);
    when(feedUpdater.stopSubscription(provider)).thenReturn(true);

    var response = controller.updateFeedProvider(SYSTEM_ID, provider);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    verify(feedUpdater).stopSubscription(provider);
  }

  @Test
  void savingAProviderAsDisabledFailsWhenItCouldNotBeCleanedUp() {
    provider.setEnabled(false);
    when(feedProviderConfig.updateProvider(provider)).thenReturn(true);
    when(feedUpdater.stopSubscription(provider)).thenReturn(false);

    var response = controller.updateFeedProvider(SYSTEM_ID, provider);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
  }

  @Test
  void savingAnEnabledProviderLeavesItsSubscriptionAlone() {
    when(feedProviderConfig.updateProvider(provider)).thenReturn(true);

    var response = controller.updateFeedProvider(SYSTEM_ID, provider);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    verify(feedUpdater, never()).stopSubscription(any());
  }

  @Test
  void savingAProviderWithoutAnEnabledValueLeavesItsSubscriptionAlone() {
    provider.setEnabled(null);
    when(feedProviderConfig.updateProvider(provider)).thenReturn(true);

    var response = controller.updateFeedProvider(SYSTEM_ID, provider);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    verify(feedUpdater, never()).stopSubscription(any());
  }
}
