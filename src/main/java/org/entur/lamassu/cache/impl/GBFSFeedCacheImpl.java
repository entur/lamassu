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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.entur.gbfs.v2_3.gbfs.GBFSFeedName;
import org.entur.lamassu.cache.GBFSFeedCache;
import org.entur.lamassu.model.provider.FeedProvider;
import org.redisson.api.RMapCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GBFSFeedCacheImpl implements GBFSFeedCache {

  private final RMapCache<String, Object> cache;

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Autowired
  public GBFSFeedCacheImpl(RMapCache<String, Object> feedCache) {
    this.cache = feedCache;
  }

  @Override
  public <T> T find(GBFSFeedName feedName, FeedProvider feedProvider) {
    var key = getKey(feedName, feedProvider.getSystemId());
    try {
      @SuppressWarnings("unchecked")
      T feed = (T) cache.getAsync(key).get(5, TimeUnit.SECONDS);
      return feed;
    } catch (ExecutionException | TimeoutException e) {
      logger.warn("Unable to fetch feed from cache within 5 second", e);
    } catch (InterruptedException e) {
      logger.warn("Interrupted while fetching feed from cache", e);
      Thread.currentThread().interrupt();
    }
    return null;
  }

  @Override
  public <T> void update(
    GBFSFeedName feedName,
    FeedProvider feedProvider,
    T feed,
    int ttl,
    TimeUnit timeUnit
  ) {
    String key = getKey(feedName, feedProvider.getSystemId());
    try {
      cache.putAsync(key, feed, ttl, timeUnit).get(5, TimeUnit.SECONDS);
    } catch (ExecutionException | TimeoutException e) {
      logger.warn("Unable to update feed cache within 5 second", e);
    } catch (InterruptedException e) {
      logger.warn("Interrupted while updating feed cache", e);
      Thread.currentThread().interrupt();
    }
  }

  @Override
  public <T> T getAndUpdate(
    GBFSFeedName feedName,
    FeedProvider feedProvider,
    T feed,
    int ttl,
    TimeUnit timeUnit
  ) {
    String key = getKey(feedName, feedProvider.getSystemId());
    try {
      @SuppressWarnings("unchecked")
      T old = (T) cache.putAsync(key, feed, ttl, timeUnit).get(5, TimeUnit.SECONDS);
      return old;
    } catch (ExecutionException | TimeoutException e) {
      logger.warn("Unable to update feed cache within 5 second", e);
    } catch (InterruptedException e) {
      logger.warn("Interrupted while updating feed cache", e);
      Thread.currentThread().interrupt();
    }
    return null;
  }

  private String getKey(GBFSFeedName feedName, String systemId) {
    return mergeStrings(feedName.value(), systemId);
  }

  private String mergeStrings(String first, String second) {
    return String.format("%s_%s", first, second);
  }
}
