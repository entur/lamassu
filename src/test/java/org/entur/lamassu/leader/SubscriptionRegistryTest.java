package org.entur.lamassu.leader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import org.entur.lamassu.cache.SubscriptionStatusCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the SubscriptionRegistry class.
 */
class SubscriptionRegistryTest {

  private SubscriptionRegistry subscriptionRegistry;
  private SubscriptionStatusCache mockCache;
  private Map<String, SubscriptionStatus> mockStorage;
  private static final String SYSTEM_ID = "test-system-id";
  private static final String SUBSCRIPTION_ID = "test-subscription-id";

  @BeforeEach
  void setUp() {
    mockCache = mock(SubscriptionStatusCache.class);
    mockStorage = new HashMap<>();

    // Mock the cache to behave like a real map
    doAnswer(invocation -> {
        String systemId = invocation.getArgument(0);
        SubscriptionStatus status = invocation.getArgument(1);
        mockStorage.put(systemId, status);
        return null;
      })
      .when(mockCache)
      .setStatus(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());

    doAnswer(invocation -> {
        String systemId = invocation.getArgument(0);
        return mockStorage.get(systemId);
      })
      .when(mockCache)
      .getStatus(org.mockito.ArgumentMatchers.any());

    doAnswer(invocation -> {
        String systemId = invocation.getArgument(0);
        mockStorage.remove(systemId);
        return null;
      })
      .when(mockCache)
      .removeStatus(org.mockito.ArgumentMatchers.any());

    doAnswer(invocation -> {
        return new HashMap<>(mockStorage);
      })
      .when(mockCache)
      .getAllStatuses();

    doAnswer(invocation -> {
        mockStorage.clear();
        return null;
      })
      .when(mockCache)
      .clear();

    subscriptionRegistry = new SubscriptionRegistry(mockCache);
  }

  @Test
  void testRegisterSubscription() {
    // Register a subscription
    subscriptionRegistry.registerSubscription(SYSTEM_ID, SUBSCRIPTION_ID);

    // Verify it was registered correctly
    assertEquals(
      SUBSCRIPTION_ID,
      subscriptionRegistry.getSubscriptionIdBySystemId(SYSTEM_ID)
    );
    assertEquals(
      SubscriptionStatus.STARTED,
      subscriptionRegistry.getSubscriptionStatusBySystemId(SYSTEM_ID)
    );
    assertTrue(subscriptionRegistry.hasSubscription(SYSTEM_ID));
  }

  @Test
  void testRemoveSubscription() {
    // Register and then remove a subscription
    subscriptionRegistry.registerSubscription(SYSTEM_ID, SUBSCRIPTION_ID);
    subscriptionRegistry.removeSubscription(SYSTEM_ID);

    // Verify it was removed correctly
    assertNull(subscriptionRegistry.getSubscriptionIdBySystemId(SYSTEM_ID));
    assertEquals(
      SubscriptionStatus.STOPPED,
      subscriptionRegistry.getSubscriptionStatusBySystemId(SYSTEM_ID)
    );
    assertFalse(subscriptionRegistry.hasSubscription(SYSTEM_ID));
  }

  @Test
  void testUpdateSubscriptionStatus() {
    // Register a subscription
    subscriptionRegistry.registerSubscription(SYSTEM_ID, SUBSCRIPTION_ID);

    // Update its status
    subscriptionRegistry.updateSubscriptionStatus(SYSTEM_ID, SubscriptionStatus.STOPPING);
    assertEquals(
      SubscriptionStatus.STOPPING,
      subscriptionRegistry.getSubscriptionStatusBySystemId(SYSTEM_ID)
    );

    subscriptionRegistry.updateSubscriptionStatus(SYSTEM_ID, SubscriptionStatus.STOPPED);
    assertEquals(
      SubscriptionStatus.STOPPED,
      subscriptionRegistry.getSubscriptionStatusBySystemId(SYSTEM_ID)
    );

    subscriptionRegistry.updateSubscriptionStatus(SYSTEM_ID, SubscriptionStatus.STARTING);
    assertEquals(
      SubscriptionStatus.STARTING,
      subscriptionRegistry.getSubscriptionStatusBySystemId(SYSTEM_ID)
    );

    subscriptionRegistry.updateSubscriptionStatus(SYSTEM_ID, SubscriptionStatus.STARTED);
    assertEquals(
      SubscriptionStatus.STARTED,
      subscriptionRegistry.getSubscriptionStatusBySystemId(SYSTEM_ID)
    );
  }

  @Test
  void testGetAllSubscriptionStatuses() {
    // Register multiple subscriptions
    subscriptionRegistry.registerSubscription(SYSTEM_ID, SUBSCRIPTION_ID);
    subscriptionRegistry.registerSubscription("system-2", "subscription-2");
    subscriptionRegistry.registerSubscription("system-3", "subscription-3");

    // Update some statuses
    subscriptionRegistry.updateSubscriptionStatus(
      "system-2",
      SubscriptionStatus.STOPPING
    );
    subscriptionRegistry.updateSubscriptionStatus("system-3", SubscriptionStatus.STOPPED);

    // Get all statuses
    Map<String, SubscriptionStatus> statuses =
      subscriptionRegistry.getAllSubscriptionStatuses();

    // Verify the map contains all expected entries with correct statuses
    assertEquals(3, statuses.size());
    assertEquals(SubscriptionStatus.STARTED, statuses.get(SYSTEM_ID));
    assertEquals(SubscriptionStatus.STOPPING, statuses.get("system-2"));
    assertEquals(SubscriptionStatus.STOPPED, statuses.get("system-3"));
  }

  @Test
  void testClear() {
    // Register multiple subscriptions
    subscriptionRegistry.registerSubscription(SYSTEM_ID, SUBSCRIPTION_ID);
    subscriptionRegistry.registerSubscription("system-2", "subscription-2");

    // Clear the registry
    subscriptionRegistry.clear();

    // Verify everything was cleared
    assertFalse(subscriptionRegistry.hasSubscription(SYSTEM_ID));
    assertFalse(subscriptionRegistry.hasSubscription("system-2"));
    assertTrue(subscriptionRegistry.getAllSubscriptionStatuses().isEmpty());
  }

  @Test
  void testDefaultStatusForNonExistentSystem() {
    // Verify that a non-existent system ID returns STOPPED status
    assertEquals(
      SubscriptionStatus.STOPPED,
      subscriptionRegistry.getSubscriptionStatusBySystemId("non-existent")
    );
  }

  @Test
  void testNullSubscriptionId() {
    // Verify that registering a null subscription ID is a no-op
    subscriptionRegistry.registerSubscription(SYSTEM_ID, null);
    assertFalse(subscriptionRegistry.hasSubscription(SYSTEM_ID));
  }

  @Test
  void testRegisterSubscriptionWritesToRedis() {
    // Register a subscription
    subscriptionRegistry.registerSubscription(SYSTEM_ID, SUBSCRIPTION_ID);

    // Verify Redis cache was updated
    verify(mockCache, times(1)).setStatus(SYSTEM_ID, SubscriptionStatus.STARTED);
  }

  @Test
  void testUpdateSubscriptionStatusWritesToRedis() {
    // Register and then update a subscription
    subscriptionRegistry.registerSubscription(SYSTEM_ID, SUBSCRIPTION_ID);
    subscriptionRegistry.updateSubscriptionStatus(SYSTEM_ID, SubscriptionStatus.STOPPING);

    // Verify Redis cache was updated twice (once for register, once for update)
    verify(mockCache, times(1)).setStatus(SYSTEM_ID, SubscriptionStatus.STARTED);
    verify(mockCache, times(1)).setStatus(SYSTEM_ID, SubscriptionStatus.STOPPING);
  }

  @Test
  void testRemoveSubscriptionRemovesFromRedis() {
    // Register and then remove a subscription
    subscriptionRegistry.registerSubscription(SYSTEM_ID, SUBSCRIPTION_ID);
    subscriptionRegistry.removeSubscription(SYSTEM_ID);

    // Verify Redis cache was updated
    verify(mockCache, times(1)).removeStatus(SYSTEM_ID);
  }

  @Test
  void testClearClearsRedis() {
    // Register some subscriptions
    subscriptionRegistry.registerSubscription(SYSTEM_ID, SUBSCRIPTION_ID);
    subscriptionRegistry.registerSubscription("system-2", "subscription-2");

    // Clear the registry
    subscriptionRegistry.clear();

    // Verify Redis cache was cleared
    verify(mockCache, times(1)).clear();
  }

  @Test
  void testFallbackToRedisWhenInMemoryEmpty() {
    // Setup: Mock Redis to return a status
    when(mockCache.getStatus("external-system")).thenReturn(SubscriptionStatus.STARTED);

    // Get status for a system not in memory (simulates follower instance)
    SubscriptionStatus status = subscriptionRegistry.getSubscriptionStatusBySystemId(
      "external-system"
    );

    // Verify it fell back to Redis
    verify(mockCache, times(1)).getStatus("external-system");
    assertEquals(SubscriptionStatus.STARTED, status);
  }

  @Test
  void testFallbackToRedisReturnsStoppedWhenNotFound() {
    // Setup: Mock Redis to return null
    when(mockCache.getStatus("unknown-system")).thenReturn(null);

    // Get status for a system not in memory or Redis
    SubscriptionStatus status = subscriptionRegistry.getSubscriptionStatusBySystemId(
      "unknown-system"
    );

    // Verify it returns STOPPED as default
    assertEquals(SubscriptionStatus.STOPPED, status);
  }
}
