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

package org.entur.lamassu.mapper.feedmapper.v2;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.entur.lamassu.mapper.feedmapper.AbstractFeedMapper;
import org.entur.lamassu.model.provider.FeedProvider;
import org.entur.lamassu.util.FeedUrlUtil;
import org.mobilitydata.gbfs.v2_3.gbfs.GBFS;
import org.mobilitydata.gbfs.v2_3.gbfs.GBFSFeed;
import org.mobilitydata.gbfs.v2_3.gbfs.GBFSFeedName;
import org.mobilitydata.gbfs.v2_3.gbfs.GBFSFeeds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DiscoveryFeedMapper extends AbstractFeedMapper<GBFS> {

  @Value("${org.entur.lamassu.baseUrl}")
  private String baseUrl;

  private static String targetGbfsVersion = "2.3";

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Override
  public GBFS map(GBFS source, FeedProvider feedProvider) {
    if (source.getFeedsData() == null) {
      logger.warn("Missing discovery data for provider={} feed={}", feedProvider, source);
      return null;
    }

    var mapped = new GBFS();
    var mappedData = new GBFSFeeds();
    Map<String, GBFSFeeds> dataWrapper = new HashMap<>();
    mapped.setLastUpdated(source.getLastUpdated());
    mapped.setTtl(source.getTtl());
    mapped.setVersion(targetGbfsVersion);

    String sourceLanguageCode;
    if (source.getFeedsData().containsKey(feedProvider.getLanguage())) {
      sourceLanguageCode = feedProvider.getLanguage();
    } else {
      sourceLanguageCode = source.getFeedsData().keySet().iterator().next();
      logger.warn(
        "Configured language code not found in discovery feed for provider {} - using {} instead",
        feedProvider,
        sourceLanguageCode
      );
    }

    var feeds = source
      .getFeedsData()
      .get(sourceLanguageCode)
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
          .noneMatch(excluded -> excluded.equals(feed.getName()))
      )
      .map(feed -> {
        var mappedFeed = new GBFSFeed();
        mappedFeed.setName(feed.getName());
        mappedFeed.setUrl(FeedUrlUtil.mapFeedUrl(baseUrl, feed.getName(), feedProvider));
        return mappedFeed;
      })
      // Lamassu currently only support producing a single version of GBFS, therefore
      // the versions file, if it exists, is intentionally skipped.
      .filter(f -> !f.getName().equals(GBFSFeedName.GBFSVersions))
      .collect(Collectors.toList());

    if (
      feedProvider.getVehicleTypes() != null &&
      feeds.stream().noneMatch(f -> f.getName().equals(GBFSFeedName.VehicleTypes))
    ) {
      var vehicleTypesFeed = new GBFSFeed();
      vehicleTypesFeed.setName(GBFSFeedName.VehicleTypes);
      vehicleTypesFeed.setUrl(
        FeedUrlUtil.mapFeedUrl(baseUrl, GBFSFeedName.VehicleTypes, feedProvider)
      );
      feeds.add(vehicleTypesFeed);
    }

    if (
      feedProvider.getPricingPlans() != null &&
      feeds.stream().noneMatch(f -> f.getName().equals(GBFSFeedName.SystemPricingPlans))
    ) {
      var pricingPlansFeed = new GBFSFeed();
      pricingPlansFeed.setName(GBFSFeedName.SystemPricingPlans);
      pricingPlansFeed.setUrl(
        FeedUrlUtil.mapFeedUrl(baseUrl, GBFSFeedName.SystemPricingPlans, feedProvider)
      );
      feeds.add(pricingPlansFeed);
    }

    mappedData.setFeeds(feeds);
    dataWrapper.put(sourceLanguageCode, mappedData);
    mapped.setFeedsData(dataWrapper);
    return mapped;
  }
}
