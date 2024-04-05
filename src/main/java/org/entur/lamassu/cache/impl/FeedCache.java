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
import org.redisson.api.RMapCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class FeedCache {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private final RMapCache<String, Object> cache;

  protected FeedCache(RMapCache<String, Object> cache) {
    this.cache = cache;
  }

  protected <T> T find(String key) {
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

  protected <T> void update(String key, T feed, int ttl, TimeUnit timeUnit) {
    try {
      cache.putAsync(key, feed, ttl, timeUnit).get(5, TimeUnit.SECONDS);
    } catch (ExecutionException | TimeoutException e) {
      logger.warn("Unable to update feed cache within 5 second", e);
    } catch (InterruptedException e) {
      logger.warn("Interrupted while updating feed cache", e);
      Thread.currentThread().interrupt();
    }
  }

  protected <T> T getAndUpdate(String key, T feed, int ttl, TimeUnit timeUnit) {
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

  protected String mergeStrings(String first, String second) {
    return String.format("%s_%s", first, second);
  }
}
