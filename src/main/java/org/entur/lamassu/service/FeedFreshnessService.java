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

package org.entur.lamassu.service;

import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import org.entur.lamassu.cache.GBFSV3FeedCache;
import org.entur.lamassu.model.provider.FeedProvider;
import org.mobilitydata.gbfs.v3_0.gbfs.GBFSFeed;
import org.mobilitydata.gbfs.v3_0.gbfs.GBFSFeedName;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Derives data-freshness signals for a feed provider from the cached GBFS feeds.
 *
 * <p>This reflects whether a system is actually receiving fresh data, independent
 * of its (durable) subscription desired-state. It reads the shared feed cache, so
 * it produces the same answer on leader and follower instances.
 */
@Service
public class FeedFreshnessService {

  private static final GBFSFeed.Name[] REALTIME_FEEDS = {
    GBFSFeed.Name.VEHICLE_STATUS,
    GBFSFeed.Name.STATION_STATUS,
  };

  private final GBFSV3FeedCache feedCache;
  private final int maxToleratedOverdueSeconds;

  public FeedFreshnessService(
    GBFSV3FeedCache feedCache,
    @Value(
      "${org.entur.lamassu.max-tolerated-overdue-seconds:120}"
    ) int maxToleratedOverdueSeconds
  ) {
    this.feedCache = feedCache;
    this.maxToleratedOverdueSeconds = maxToleratedOverdueSeconds;
  }

  /**
   * A system is considered live if at least one of its realtime feeds
   * (vehicle_status or station_status) is present in the cache and not overdue.
   */
  public boolean isLive(FeedProvider feedProvider) {
    for (GBFSFeed.Name feedName : REALTIME_FEEDS) {
      if (isFeedFresh(feedProvider, feedName)) {
        return true;
      }
    }
    return false;
  }

  /**
   * The most recent last_updated timestamp across the system's realtime feeds,
   * or empty if none are present.
   */
  public Optional<Instant> lastUpdated(FeedProvider feedProvider) {
    Instant latest = null;
    for (GBFSFeed.Name feedName : REALTIME_FEEDS) {
      Instant timestamp = feedLastUpdated(feedProvider, feedName);
      if (timestamp != null && (latest == null || timestamp.isAfter(latest))) {
        latest = timestamp;
      }
    }
    return Optional.ofNullable(latest);
  }

  /**
   * Whether a specific feed file is overdue (present but past its ttl plus the
   * tolerated overdue window). Absent feeds are not considered overdue.
   */
  public boolean isFeedOverdue(FeedProvider feedProvider, GBFSFeed.Name feedName) {
    Object feed = feedCache.find(feedName, feedProvider);
    if (feed != null) {
      long absoluteTtl = getAbsoluteTtl(GBFSFeedName.implementingClass(feedName), feed);
      return absoluteTtl + maxToleratedOverdueSeconds < Instant.now().getEpochSecond();
    }
    return false;
  }

  private boolean isFeedFresh(FeedProvider feedProvider, GBFSFeed.Name feedName) {
    Object feed = feedCache.find(feedName, feedProvider);
    if (feed == null) {
      return false;
    }
    long absoluteTtl = getAbsoluteTtl(GBFSFeedName.implementingClass(feedName), feed);
    return absoluteTtl + maxToleratedOverdueSeconds >= Instant.now().getEpochSecond();
  }

  private Instant feedLastUpdated(FeedProvider feedProvider, GBFSFeed.Name feedName) {
    Object feed = feedCache.find(feedName, feedProvider);
    if (feed == null) {
      return null;
    }
    try {
      Date lastUpdated = (Date) GBFSFeedName
        .implementingClass(feedName)
        .getMethod("getLastUpdated")
        .invoke(feed);
      return lastUpdated == null
        ? null
        : Instant.ofEpochSecond(lastUpdated.getTime() / 1000);
    } catch (
      NoSuchMethodException | InvocationTargetException | IllegalAccessException e
    ) {
      return null;
    }
  }

  private <T> long getAbsoluteTtl(Class<?> feedClass, T feed) {
    try {
      Date lastUpdated = (Date) feedClass.getMethod("getLastUpdated").invoke(feed);
      Integer ttl = (Integer) feedClass.getMethod("getTtl").invoke(feed);
      return lastUpdated.getTime() / 1000 + ttl;
    } catch (
      NoSuchMethodException | InvocationTargetException | IllegalAccessException e
    ) {
      return 0;
    }
  }
}
