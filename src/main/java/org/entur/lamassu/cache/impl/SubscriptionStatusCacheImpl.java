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

import java.util.HashMap;
import java.util.Map;
import org.entur.lamassu.cache.SubscriptionStatusCache;
import org.entur.lamassu.leader.SubscriptionStatus;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

/**
 * Redis-backed implementation of SubscriptionStatusCache.
 * Uses Redisson RMap for thread-safe Redis operations with Kryo serialization.
 */
@Component
public class SubscriptionStatusCacheImpl implements SubscriptionStatusCache {

  private static final String CACHE_KEY = "subscription-status";
  private final RMap<String, SubscriptionStatus> cache;

  public SubscriptionStatusCacheImpl(RedissonClient redissonClient) {
    this.cache = redissonClient.getMap(CACHE_KEY);
  }

  @Override
  public void setStatus(String systemId, SubscriptionStatus status) {
    if (systemId == null || status == null) {
      return;
    }
    cache.put(systemId, status);
  }

  @Override
  public SubscriptionStatus getStatus(String systemId) {
    if (systemId == null) {
      return null;
    }
    return cache.get(systemId);
  }

  @Override
  public Map<String, SubscriptionStatus> getAllStatuses() {
    return new HashMap<>(cache.readAllMap());
  }

  @Override
  public void removeStatus(String systemId) {
    if (systemId == null) {
      return;
    }
    cache.remove(systemId);
  }

  @Override
  public void clear() {
    cache.clear();
  }
}
