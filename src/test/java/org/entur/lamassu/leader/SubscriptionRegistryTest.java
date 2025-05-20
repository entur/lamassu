package org.entur.lamassu.leader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the SubscriptionRegistry class.
 */
public class SubscriptionRegistryTest {

  private SubscriptionRegistry subscriptionRegistry;
  private static final String SYSTEM_ID = "test-system-id";
  private static final String SUBSCRIPTION_ID = "test-subscription-id";

  @BeforeEach
  public void setUp() {
    subscriptionRegistry = new SubscriptionRegistry();
  }

  @Test
  public void testRegisterSubscription() {
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
  public void testRemoveSubscription() {
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
  public void testUpdateSubscriptionStatus() {
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
  public void testGetAllSubscriptionStatuses() {
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
  public void testClear() {
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
  public void testDefaultStatusForNonExistentSystem() {
    // Verify that a non-existent system ID returns STOPPED status
    assertEquals(
      SubscriptionStatus.STOPPED,
      subscriptionRegistry.getSubscriptionStatusBySystemId("non-existent")
    );
  }

  @Test
  public void testNullSubscriptionId() {
    // Verify that registering a null subscription ID is a no-op
    subscriptionRegistry.registerSubscription(SYSTEM_ID, null);
    assertFalse(subscriptionRegistry.hasSubscription(SYSTEM_ID));
  }
}
