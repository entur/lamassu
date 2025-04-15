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

import java.net.URI;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Registry for tracking subscription IDs and statuses by feed provider system ID and URL.
 */
@Component
public class SubscriptionRegistry {

  private static final Logger logger = LoggerFactory.getLogger(SubscriptionRegistry.class);

  private final Map<String, String> subscriptionIdsBySystemId = new ConcurrentHashMap<>();
  private final Map<String, SubscriptionStatus> subscriptionStatusBySystemId = new ConcurrentHashMap<>();

  /**
   * Registers a subscription ID for a system ID and URL.
   *
   * @param systemId The system ID of the feed provider
   * @param url The URL of the feed provider
   * @param subscriptionId The subscription ID
   */
  public void registerSubscription(String systemId, URI url, String subscriptionId) {
    if (subscriptionId != null) {
      subscriptionIdsBySystemId.put(systemId, subscriptionId);
      subscriptionStatusBySystemId.put(systemId, SubscriptionStatus.STARTED);
      logger.debug("Registered subscription ID {} for system ID {} and URL {}", subscriptionId, systemId, url);
    }
  }

  /**
   * Removes a subscription for a system ID and URL.
   *
   * @param systemId The system ID of the feed provider
   * @param url The URL of the feed provider
   */
  public void removeSubscription(String systemId, URI url) {
    subscriptionIdsBySystemId.remove(systemId);
    subscriptionStatusBySystemId.remove(systemId);
    logger.debug("Removed subscription for system ID {} and URL {}", systemId, url);
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
   *
   * @param systemId The system ID of the feed provider
   * @return The subscription status, or null if not found
   */
  public SubscriptionStatus getSubscriptionStatusBySystemId(String systemId) {
    return subscriptionStatusBySystemId.getOrDefault(systemId, SubscriptionStatus.STOPPED);
  }

  /**
   * Updates the subscription status for a system ID.
   *
   * @param systemId The system ID of the feed provider
   * @param status The new subscription status
   */
  public void updateSubscriptionStatus(String systemId, SubscriptionStatus status) {
    subscriptionStatusBySystemId.put(systemId, status);
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
    logger.debug("Cleared all subscription registrations");
  }
}
