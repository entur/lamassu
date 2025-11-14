/*
 *
 *
 *  * Licensed under the EUPL, Version 1.2 or u2013 as soon they will be approved by
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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.entur.lamassu.cache.SubscriptionStatusCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Registry for tracking subscription IDs and statuses by feed provider system ID.
 * Subscription IDs are stored in-memory only (leader-specific).
 * Subscription statuses are stored both in-memory (for performance) and in Redis (for cross-instance visibility).
 */
@Component
public class SubscriptionRegistry {

  private static final Logger logger = LoggerFactory.getLogger(
    SubscriptionRegistry.class
  );

  private final Map<String, String> subscriptionIdsBySystemId = new ConcurrentHashMap<>();
  private final Map<String, SubscriptionStatus> subscriptionStatusBySystemId =
    new ConcurrentHashMap<>();
  private final SubscriptionStatusCache subscriptionStatusCache;

  public SubscriptionRegistry(SubscriptionStatusCache subscriptionStatusCache) {
    this.subscriptionStatusCache = subscriptionStatusCache;
  }

  /**
   * Registers a subscription ID for a system ID.
   *
   * @param systemId The system ID of the feed provider
   * @param subscriptionId The subscription ID
   */
  public void registerSubscription(String systemId, String subscriptionId) {
    if (subscriptionId != null) {
      subscriptionIdsBySystemId.put(systemId, subscriptionId);
      subscriptionStatusBySystemId.put(systemId, SubscriptionStatus.STARTED);
      subscriptionStatusCache.setStatus(systemId, SubscriptionStatus.STARTED);
      logger.debug(
        "Registered subscription ID {} for system ID {}",
        subscriptionId,
        systemId
      );
    }
  }

  /**
   * Removes a subscription for a system ID.
   *
   * @param systemId The system ID of the feed provider
   */
  public void removeSubscription(String systemId) {
    subscriptionIdsBySystemId.remove(systemId);
    subscriptionStatusBySystemId.remove(systemId);
    subscriptionStatusCache.removeStatus(systemId);
    logger.debug("Removed subscription for system ID {}", systemId);
  }

  /**
   * Gets the subscription ID for a system ID.
   *
   * @param systemId The system ID of the feed provider
   * @return The subscription ID, or null if not found
   */
  public String getSubscriptionIdBySystemId(String systemId) {
    return subscriptionIdsBySystemId.get(systemId);
  }

  /**
   * Gets the subscription status for a system ID.
   * First checks in-memory map (for leader instance performance),
   * then falls back to Redis (for follower instances).
   *
   * @param systemId The system ID of the feed provider
   * @return The subscription status, or STOPPED if not found
   */
  public SubscriptionStatus getSubscriptionStatusBySystemId(String systemId) {
    // Check in-memory first (leader instance)
    SubscriptionStatus status = subscriptionStatusBySystemId.get(systemId);
    if (status != null) {
      return status;
    }

    // Fall back to Redis (follower instances)
    status = subscriptionStatusCache.getStatus(systemId);
    return status != null ? status : SubscriptionStatus.STOPPED;
  }

  /**
   * Updates the subscription status for a system ID.
   *
   * @param systemId The system ID of the feed provider
   * @param status The new subscription status
   */
  public void updateSubscriptionStatus(String systemId, SubscriptionStatus status) {
    subscriptionStatusBySystemId.put(systemId, status);
    subscriptionStatusCache.setStatus(systemId, status);
    logger.debug("Updated subscription status for system ID {} to {}", systemId, status);
  }

  /**
   * Checks if a subscription exists for a system ID.
   *
   * @param systemId The system ID of the feed provider
   * @return true if a subscription exists, false otherwise
   */
  public boolean hasSubscription(String systemId) {
    return subscriptionIdsBySystemId.containsKey(systemId);
  }

  /**
   * Gets all subscription statuses.
   *
   * @return A map of system IDs to subscription statuses
   */
  public Map<String, SubscriptionStatus> getAllSubscriptionStatuses() {
    return new HashMap<>(subscriptionStatusBySystemId);
  }

  /**
   * Clears all registered subscriptions.
   */
  public void clear() {
    subscriptionIdsBySystemId.clear();
    subscriptionStatusBySystemId.clear();
    subscriptionStatusCache.clear();
    logger.debug("Cleared all subscription registrations");
  }
}
