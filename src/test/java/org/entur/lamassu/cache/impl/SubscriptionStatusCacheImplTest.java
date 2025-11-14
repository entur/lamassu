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

package org.entur.lamassu.cache.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.entur.lamassu.TestLamassuApplication;
import org.entur.lamassu.cache.SubscriptionStatusCache;
import org.entur.lamassu.leader.SubscriptionStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Integration tests for SubscriptionStatusCacheImpl using embedded Redis.
 */
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = TestLamassuApplication.class)
class SubscriptionStatusCacheImplTest {

  @Autowired
  private SubscriptionStatusCache subscriptionStatusCache;

  @AfterEach
  void cleanup() {
    subscriptionStatusCache.clear();
  }

  @Test
  void testSetAndGetStatus() {
    String systemId = "test-system";
    SubscriptionStatus status = SubscriptionStatus.STARTED;

    subscriptionStatusCache.setStatus(systemId, status);

    assertEquals(status, subscriptionStatusCache.getStatus(systemId));
  }

  @Test
  void testGetStatusReturnsNullWhenNotFound() {
    String systemId = "non-existent-system";

    assertNull(subscriptionStatusCache.getStatus(systemId));
  }

  @Test
  void testSetStatusWithNullSystemId() {
    // Should not throw exception
    subscriptionStatusCache.setStatus(null, SubscriptionStatus.STARTED);

    // Verify no status was stored
    Map<String, SubscriptionStatus> allStatuses =
      subscriptionStatusCache.getAllStatuses();
    assertTrue(allStatuses.isEmpty());
  }

  @Test
  void testSetStatusWithNullStatus() {
    String systemId = "test-system";

    // Should not throw exception
    subscriptionStatusCache.setStatus(systemId, null);

    // Verify no status was stored
    assertNull(subscriptionStatusCache.getStatus(systemId));
  }

  @Test
  void testGetStatusWithNullSystemId() {
    // Should not throw exception
    assertNull(subscriptionStatusCache.getStatus(null));
  }

  @Test
  void testGetAllStatuses() {
    subscriptionStatusCache.setStatus("system-1", SubscriptionStatus.STARTED);
    subscriptionStatusCache.setStatus("system-2", SubscriptionStatus.STOPPED);
    subscriptionStatusCache.setStatus("system-3", SubscriptionStatus.STARTING);

    Map<String, SubscriptionStatus> allStatuses =
      subscriptionStatusCache.getAllStatuses();

    assertEquals(3, allStatuses.size());
    assertEquals(SubscriptionStatus.STARTED, allStatuses.get("system-1"));
    assertEquals(SubscriptionStatus.STOPPED, allStatuses.get("system-2"));
    assertEquals(SubscriptionStatus.STARTING, allStatuses.get("system-3"));
  }

  @Test
  void testGetAllStatusesReturnsEmptyMapWhenNoStatuses() {
    Map<String, SubscriptionStatus> allStatuses =
      subscriptionStatusCache.getAllStatuses();

    assertTrue(allStatuses.isEmpty());
  }

  @Test
  void testRemoveStatus() {
    String systemId = "test-system";
    subscriptionStatusCache.setStatus(systemId, SubscriptionStatus.STARTED);

    subscriptionStatusCache.removeStatus(systemId);

    assertNull(subscriptionStatusCache.getStatus(systemId));
  }

  @Test
  void testRemoveStatusWithNullSystemId() {
    // Should not throw exception
    subscriptionStatusCache.removeStatus(null);
  }

  @Test
  void testClear() {
    subscriptionStatusCache.setStatus("system-1", SubscriptionStatus.STARTED);
    subscriptionStatusCache.setStatus("system-2", SubscriptionStatus.STOPPED);

    subscriptionStatusCache.clear();

    assertTrue(subscriptionStatusCache.getAllStatuses().isEmpty());
    assertNull(subscriptionStatusCache.getStatus("system-1"));
    assertNull(subscriptionStatusCache.getStatus("system-2"));
  }

  @Test
  void testUpdateExistingStatus() {
    String systemId = "test-system";
    subscriptionStatusCache.setStatus(systemId, SubscriptionStatus.STARTED);

    // Update the status
    subscriptionStatusCache.setStatus(systemId, SubscriptionStatus.STOPPING);

    assertEquals(
      SubscriptionStatus.STOPPING,
      subscriptionStatusCache.getStatus(systemId)
    );
  }

  @Test
  void testAllStatusEnums() {
    // Verify all enum values can be stored and retrieved
    String systemId = "test-system";

    for (SubscriptionStatus status : SubscriptionStatus.values()) {
      subscriptionStatusCache.setStatus(systemId, status);
      assertEquals(status, subscriptionStatusCache.getStatus(systemId));
    }
  }
}
