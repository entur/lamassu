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

package org.entur.lamassu.cache;

import java.util.Map;
import org.entur.lamassu.leader.SubscriptionStatus;

/**
 * Cache interface for storing subscription status in Redis.
 * This allows all instances (leader and followers) to access subscription status.
 */
public interface SubscriptionStatusCache {
  /**
   * Store status in Redis.
   *
   * @param systemId The system ID of the feed provider
   * @param status The subscription status to store
   */
  void setStatus(String systemId, SubscriptionStatus status);

  /**
   * Retrieve status from Redis.
   *
   * @param systemId The system ID of the feed provider
   * @return The subscription status, or null if not found
   */
  SubscriptionStatus getStatus(String systemId);

  /**
   * Get all statuses from Redis.
   *
   * @return Map of system IDs to subscription statuses
   */
  Map<String, SubscriptionStatus> getAllStatuses();

  /**
   * Remove status from Redis.
   *
   * @param systemId The system ID of the feed provider
   */
  void removeStatus(String systemId);

  /**
   * Clear all statuses from Redis.
   */
  void clear();
}
