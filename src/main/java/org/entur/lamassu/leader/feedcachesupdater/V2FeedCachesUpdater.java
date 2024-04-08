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

package org.entur.lamassu.leader.feedcachesupdater;

import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import org.entur.gbfs.loader.v2.GbfsV2Delivery;
import org.entur.gbfs.v2_3.gbfs.GBFSFeedName;
import org.entur.lamassu.cache.GBFSV2FeedCache;
import org.entur.lamassu.model.provider.FeedProvider;
import org.entur.lamassu.util.CacheUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Class responsible for updating cache of GBFS v2 feeds
 */
@Component
public class V2FeedCachesUpdater {

  public static final int MINIMUM_TTL = 86400;
  private final GBFSV2FeedCache feedCache;
  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Value("${org.entur.lamassu.feedCacheTtlPadding:3600}")
  private Integer feedCacheTtlPadding;

  @Value("${org.entur.lamassu.feedCacheMinimumTtl:3600}")
  private Integer feedCacheMinimumTtl;

  @Autowired
  public V2FeedCachesUpdater(GBFSV2FeedCache feedCache) {
    this.feedCache = feedCache;
  }

  public GbfsV2Delivery updateFeedCaches(
    FeedProvider feedProvider,
    GbfsV2Delivery delivery
  ) {
    updateFeedCache(feedProvider, GBFSFeedName.GBFS, delivery.discovery());
    updateFeedCache(
      feedProvider,
      GBFSFeedName.SystemInformation,
      delivery.systemInformation()
    );
    updateFeedCache(feedProvider, GBFSFeedName.SystemAlerts, delivery.systemAlerts());
    updateFeedCache(feedProvider, GBFSFeedName.SystemCalendar, delivery.systemCalendar());
    updateFeedCache(feedProvider, GBFSFeedName.SystemRegions, delivery.systemRegions());
    updateFeedCache(
      feedProvider,
      GBFSFeedName.SystemPricingPlans,
      delivery.systemPricingPlans()
    );
    updateFeedCache(feedProvider, GBFSFeedName.SystemHours, delivery.systemHours());
    updateFeedCache(feedProvider, GBFSFeedName.VehicleTypes, delivery.vehicleTypes());
    updateFeedCache(
      feedProvider,
      GBFSFeedName.GeofencingZones,
      delivery.geofencingZones()
    );
    updateFeedCache(
      feedProvider,
      GBFSFeedName.StationInformation,
      delivery.stationInformation()
    );
    var oldStationStatus = getAndUpdateFeedCache(
      feedProvider,
      GBFSFeedName.StationStatus,
      delivery.stationStatus()
    );
    var oldFreeBikeStatus = getAndUpdateFeedCache(
      feedProvider,
      GBFSFeedName.FreeBikeStatus,
      delivery.freeBikeStatus()
    );
    return new GbfsV2Delivery(
      null,
      null,
      null,
      null,
      null,
      oldStationStatus,
      oldFreeBikeStatus,
      null,
      null,
      null,
      null,
      null,
      null,
      null
    );
  }

  private <T> void updateFeedCache(
    FeedProvider feedProvider,
    GBFSFeedName feedName,
    T feed
  ) {
    if (shouldIncludeFeed(feedProvider, feedName, feed)) {
      logger.debug(
        "updating feed {} for provider {}",
        feedName,
        feedProvider.getSystemId()
      );
      logger.trace(
        "updating feed {} for provider {} data {}",
        feedName,
        feedProvider.getSystemId(),
        feed
      );
      var ttl = getTtl(feedName.implementingClass(), feed, feedCacheMinimumTtl);
      feedCache.update(
        feedName,
        feedProvider,
        feed,
        ttl + feedCacheTtlPadding,
        TimeUnit.SECONDS
      );
    } else {
      logger.debug(
        "no feed {} found for provider {}",
        feedName,
        feedProvider.getSystemId()
      );
    }
  }

  private <T> int getTtl(Class<?> implementingClass, T feed, int minimumTtl) {
    try {
      Integer lastUpdated = (Integer) implementingClass
        .getMethod("getLastUpdated")
        .invoke(feed);
      Integer ttl = (Integer) implementingClass.getMethod("getTtl").invoke(feed);
      return CacheUtil.getTtl(
        (int) Instant.now().getEpochSecond(),
        lastUpdated,
        ttl,
        minimumTtl
      );
    } catch (
      NoSuchMethodException | InvocationTargetException | IllegalAccessException e
    ) {
      logger.warn("Unable to determine ttl for feed, using default - {}", feed);
      return minimumTtl;
    }
  }

  private <T> T getAndUpdateFeedCache(
    FeedProvider feedProvider,
    GBFSFeedName feedName,
    T feed
  ) {
    if (shouldIncludeFeed(feedProvider, feedName, feed)) {
      logger.debug(
        "updating feed {} for provider {}",
        feedName,
        feedProvider.getSystemId()
      );
      logger.trace(
        "updating feed {} for provider {} data {}",
        feedName,
        feedProvider.getSystemId(),
        feed
      );
      var ttl = getTtl(feedName.implementingClass(), feed, MINIMUM_TTL);
      return feedCache.getAndUpdate(feedName, feedProvider, feed, ttl, TimeUnit.SECONDS);
    } else {
      logger.debug(
        "no feed {} found for provider {}",
        feedName,
        feedProvider.getSystemId()
      );
      return null;
    }
  }

  private <T> boolean shouldIncludeFeed(
    FeedProvider feedProvider,
    GBFSFeedName feedName,
    T feed
  ) {
    return (
      feed != null &&
      (
        feedProvider.getExcludeFeeds() == null ||
        !feedProvider.getExcludeFeeds().contains(feedName)
      )
    );
  }
}
