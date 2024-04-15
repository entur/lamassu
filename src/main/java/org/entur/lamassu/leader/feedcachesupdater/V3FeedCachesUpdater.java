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
import java.util.Date;
import java.util.concurrent.TimeUnit;
import org.entur.gbfs.loader.v3.GbfsV3Delivery;
import org.entur.gbfs.mapper.GBFSFeedNameMapper;
import org.entur.gbfs.v3_0.gbfs.GBFSFeed;
import org.entur.gbfs.v3_0.gbfs.GBFSFeed.Name;
import org.entur.gbfs.v3_0.gbfs.GBFSFeedName;
import org.entur.lamassu.cache.GBFSV3FeedCache;
import org.entur.lamassu.model.provider.FeedProvider;
import org.entur.lamassu.util.CacheUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Class responsible for updating cache of GBFS v3 feeds
 */
@Component
public class V3FeedCachesUpdater {

  public static final int MINIMUM_TTL = 86400;
  private final GBFSV3FeedCache feedCache;
  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Value("${org.entur.lamassu.feedCacheTtlPadding:3600}")
  private Integer feedCacheTtlPadding;

  @Value("${org.entur.lamassu.feedCacheMinimumTtl:3600}")
  private Integer feedCacheMinimumTtl;

  @Autowired
  public V3FeedCachesUpdater(GBFSV3FeedCache feedCache) {
    this.feedCache = feedCache;
  }

  public GbfsV3Delivery updateFeedCaches(
    FeedProvider feedProvider,
    GbfsV3Delivery delivery
  ) {
    updateFeedCache(feedProvider, GBFSFeed.Name.GBFS, delivery.discovery());
    updateFeedCache(feedProvider, Name.SYSTEM_INFORMATION, delivery.systemInformation());
    updateFeedCache(feedProvider, Name.SYSTEM_ALERTS, delivery.systemAlerts());
    updateFeedCache(feedProvider, Name.SYSTEM_REGIONS, delivery.systemRegions());
    updateFeedCache(
      feedProvider,
      Name.SYSTEM_PRICING_PLANS,
      delivery.systemPricingPlans()
    );
    updateFeedCache(feedProvider, Name.VEHICLE_TYPES, delivery.vehicleTypes());
    updateFeedCache(feedProvider, Name.GEOFENCING_ZONES, delivery.geofencingZones());
    updateFeedCache(
      feedProvider,
      Name.STATION_INFORMATION,
      delivery.stationInformation()
    );
    var oldStationStatus = getAndUpdateFeedCache(
      feedProvider,
      Name.STATION_STATUS,
      delivery.stationStatus()
    );
    var oldVehicleStatus = getAndUpdateFeedCache(
      feedProvider,
      Name.VEHICLE_STATUS,
      delivery.vehicleStatus()
    );
    return new GbfsV3Delivery(
      null,
      null,
      null,
      null,
      null,
      oldStationStatus,
      oldVehicleStatus,
      null,
      null,
      null,
      null,
      null
    );
  }

  private <T> void updateFeedCache(
    FeedProvider feedProvider,
    GBFSFeed.Name feedName,
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

      var ttl = getTtl(
        GBFSFeedName.implementingClass(feedName),
        feed,
        feedCacheMinimumTtl
      );
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
      Date lastUpdated = (Date) implementingClass
        .getMethod("getLastUpdated")
        .invoke(feed);
      Integer ttl = (Integer) implementingClass.getMethod("getTtl").invoke(feed);
      return CacheUtil.getTtl(
        (int) Instant.now().getEpochSecond(),
        (int) (lastUpdated.getTime() / 1000),
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
    GBFSFeed.Name feedName,
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
      var ttl = getTtl(GBFSFeedName.implementingClass(feedName), feed, MINIMUM_TTL);
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
    GBFSFeed.Name feedName,
    T feed
  ) {
    return (
      feed != null &&
      (
        feedProvider.getExcludeFeeds() == null ||
        !feedProvider
          .getExcludeFeeds()
          .contains(GBFSFeedNameMapper.INSTANCE.map(feedName))
      )
    );
  }
}
