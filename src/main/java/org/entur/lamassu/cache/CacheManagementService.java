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

package org.entur.lamassu.cache;

import java.util.Collection;
import java.util.List;
import org.redisson.api.RFuture;

/**
 * Service interface for managing cache operations.
 * This includes operations like clearing old caches, listing cache keys,
 * and other cache management functions.
 */
public interface CacheManagementService {
  /**
   * Gets all cache keys currently in the cache store.
   *
   * @return A collection of all cache keys
   */
  Collection<String> getCacheKeys();

  /**
   * Clears all caches in the system.
   *
   * @return A future that completes when the operation is done
   */
  RFuture<Void> clearAllCaches();

  /**
   * Clears old cache entries that are no longer needed.
   * This preserves essential configuration data that should not be deleted.
   *
   * @return A list of keys that were deleted
   */
  List<String> clearOldCaches();
}
