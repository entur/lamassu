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

import org.entur.gbfs.v3_0_RC2.gbfs.GBFSFeed;
import org.entur.lamassu.cache.GBFSV3FeedCache;
import org.entur.lamassu.model.provider.FeedProvider;
import org.redisson.api.RMapCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class GBFSV3FeedCacheImpl extends FeedCache implements GBFSV3FeedCache {

  @Autowired
  public GBFSV3FeedCacheImpl(RMapCache<String, Object> v3FeedCache) {
    super(v3FeedCache);
  }

  @Override
  public <T> T find(GBFSFeed.Name feedName, FeedProvider feedProvider) {
    var key = getKey(feedName, feedProvider.getSystemId());
    return find(key);
  }

  @Override
  public <T> void update(
    GBFSFeed.Name feedName,
    FeedProvider feedProvider,
    T feed,
    int ttl,
    TimeUnit timeUnit
  ) {
    String key = getKey(feedName, feedProvider.getSystemId());
    update(key, feed, ttl, timeUnit);
  }

  @Override
  public <T> T getAndUpdate(
    GBFSFeed.Name feedName,
    FeedProvider feedProvider,
    T feed,
    int ttl,
    TimeUnit timeUnit
  ) {
    String key = getKey(feedName, feedProvider.getSystemId());
    return getAndUpdate(key, feed, ttl, timeUnit);
  }

  private String getKey(GBFSFeed.Name feedName, String systemId) {
    return mergeStrings(feedName.value(), systemId);
  }
}
