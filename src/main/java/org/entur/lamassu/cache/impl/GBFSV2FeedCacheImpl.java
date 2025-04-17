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

import java.util.concurrent.TimeUnit;
import org.entur.lamassu.cache.GBFSV2FeedCache;
import org.entur.lamassu.model.provider.FeedProvider;
import org.mobilitydata.gbfs.v2_3.gbfs.GBFSFeedName;
import org.redisson.api.RMapCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GBFSV2FeedCacheImpl extends FeedCache implements GBFSV2FeedCache {

  @Autowired
  public GBFSV2FeedCacheImpl(RMapCache<String, Object> feedCache) {
    super(feedCache);
  }

  @Override
  public <T> T find(GBFSFeedName feedName, FeedProvider feedProvider) {
    var key = getKey(feedName, feedProvider.getSystemId());
    return find(key);
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
    update(key, feed, ttl, timeUnit);
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
    return getAndUpdate(key, feed, ttl, timeUnit);
  }

  @Override
  public void remove(GBFSFeedName feedName, FeedProvider feedProvider) {
    String key = getKey(feedName, feedProvider.getSystemId());
    remove(key);
  }

  private String getKey(GBFSFeedName feedName, String systemId) {
    return mergeStrings(feedName.value(), systemId);
  }
}
