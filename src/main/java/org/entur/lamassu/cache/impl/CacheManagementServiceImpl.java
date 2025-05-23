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

package org.entur.lamassu.cache.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.StreamSupport;
import org.entur.lamassu.cache.CacheManagementService;
import org.entur.lamassu.config.feedprovider.FeedProviderConfigRedis;
import org.entur.lamassu.config.project.LamassuProjectInfoConfiguration;
import org.redisson.api.RFuture;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementation of the CacheManagementService interface.
 * This service provides methods for managing cache operations using Redisson.
 */
@Service
public class CacheManagementServiceImpl implements CacheManagementService {

  private final RedissonClient redissonClient;
  private final String serializationVersion;
  private final List<String> protectedKeys;

  @Autowired
  public CacheManagementServiceImpl(
    RedissonClient redissonClient,
    LamassuProjectInfoConfiguration lamassuProjectInfoConfiguration
  ) {
    this.redissonClient = redissonClient;
    this.serializationVersion = lamassuProjectInfoConfiguration.getSerializationVersion();

    // Initialize the list of keys that should be protected from deletion
    this.protectedKeys = new ArrayList<>();
    this.protectedKeys.add(FeedProviderConfigRedis.FEED_PROVIDERS_REDIS_KEY); // Protect feed provider configuration
  }

  @Override
  public Collection<String> getCacheKeys() {
    return StreamSupport
      .stream(redissonClient.getKeys().getKeys().spliterator(), false)
      .toList();
  }

  @Override
  public RFuture<Void> clearAllCaches() {
    return redissonClient.getKeys().flushdbParallelAsync();
  }

  @Override
  public List<String> clearOldCaches() {
    var keys = redissonClient.getKeys();
    List<String> deletedKeys = new ArrayList<>();

    keys
      .getKeys()
      .forEach(key -> {
        // Skip keys that end with the current serialization version
        // and keys that are in the protected list
        if (!key.endsWith("_" + serializationVersion) && !protectedKeys.contains(key)) {
          keys.delete(key);
          deletedKeys.add(key);
        }
      });

    return deletedKeys;
  }
}
