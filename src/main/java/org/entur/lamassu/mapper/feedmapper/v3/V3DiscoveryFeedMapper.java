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

package org.entur.lamassu.mapper.feedmapper.v3;

import java.util.stream.Collectors;
import org.entur.gbfs.v3_0_RC2.gbfs.GBFSData;
import org.entur.gbfs.v3_0_RC2.gbfs.GBFSFeed;
import org.entur.gbfs.v3_0_RC2.gbfs.GBFSGbfs;
import org.entur.lamassu.mapper.feedmapper.AbstractFeedMapper;
import org.entur.lamassu.model.provider.FeedProvider;
import org.entur.lamassu.util.FeedUrlUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class V3DiscoveryFeedMapper extends AbstractFeedMapper<GBFSGbfs> {

  private static final GBFSGbfs.Version TARGET_GBFS_VERSION = GBFSGbfs.Version._3_0_RC_2;

  @Value("${org.entur.lamassu.baseUrl}")
  private String baseUrl;

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Override
  public GBFSGbfs map(GBFSGbfs source, FeedProvider feedProvider) {
    if (source.getData() == null) {
      logger.warn("Missing discovery data for provider={} feed={}", feedProvider, source);
      return null;
    }

    var mapped = new GBFSGbfs();
    var mappedData = new GBFSData();

    mapped.setLastUpdated(source.getLastUpdated());
    mapped.setTtl(source.getTtl());
    mapped.setVersion(TARGET_GBFS_VERSION);

    var feeds = source
      .getData()
      .getFeeds()
      .stream()
      .filter(feed -> {
        if (feed.getName() == null) {
          logger.info("Skipped non-standard file {}", feed.getUrl());
        }
        return feed.getName() != null;
      })
      .filter(feed ->
        feedProvider.getExcludeFeeds() == null ||
        feedProvider
          .getExcludeFeeds()
          .stream()
          // TODO there should be a mapper between the GBFSFeedName and GBFSFeed.Name enums
          // since this solution does not work for free_bike_status / vehicle_status
          // and will possibly crash for system_calendar and system_hours
          .noneMatch(excluded ->
            excluded.equals(
              org.entur.gbfs.v2_3.gbfs.GBFSFeedName.fromValue(feed.getName().toString())
            )
          )
      )
      .map(feed -> {
        var mappedFeed = new GBFSFeed();
        mappedFeed.setName(feed.getName());
        mappedFeed.setUrl(FeedUrlUtil.mapFeedUrl(baseUrl, feed.getName(), feedProvider));
        return mappedFeed;
      })
      // Lamassu currently only support producing a single version of GBFS, therefore
      // the versions file, if it exists, is intentionally skipped.
      .filter(f -> !f.getName().equals(GBFSFeed.Name.GBFS_VERSIONS))
      .collect(Collectors.toList());

    if (
      feedProvider.getVehicleTypes() != null &&
      feeds.stream().noneMatch(f -> f.getName().equals(GBFSFeed.Name.VEHICLE_TYPES))
    ) {
      var vehicleTypesFeed = new GBFSFeed();
      vehicleTypesFeed.setName(GBFSFeed.Name.VEHICLE_TYPES);
      vehicleTypesFeed.setUrl(
        FeedUrlUtil.mapFeedUrl(baseUrl, GBFSFeed.Name.VEHICLE_TYPES, feedProvider)
      );
      feeds.add(vehicleTypesFeed);
    }

    if (
      feedProvider.getPricingPlans() != null &&
      feeds
        .stream()
        .noneMatch(f -> f.getName().equals(GBFSFeed.Name.SYSTEM_PRICING_PLANS))
    ) {
      var pricingPlansFeed = new GBFSFeed();
      pricingPlansFeed.setName(GBFSFeed.Name.SYSTEM_PRICING_PLANS);
      pricingPlansFeed.setUrl(
        FeedUrlUtil.mapFeedUrl(baseUrl, GBFSFeed.Name.SYSTEM_PRICING_PLANS, feedProvider)
      );
      feeds.add(pricingPlansFeed);
    }

    mappedData.setFeeds(feeds);
    mapped.setData(mappedData);
    return mapped;
  }
}
