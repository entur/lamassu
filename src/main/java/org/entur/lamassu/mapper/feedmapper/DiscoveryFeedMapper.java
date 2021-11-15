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

package org.entur.lamassu.mapper.feedmapper;

import org.entur.gbfs.v2_2.gbfs.GBFS;
import org.entur.gbfs.v2_2.gbfs.GBFSFeed;
import org.entur.gbfs.v2_2.gbfs.GBFSFeedName;
import org.entur.gbfs.v2_2.gbfs.GBFSFeeds;
import org.entur.gbfs.v2_2.gbfs.GBFSGbfs;
import org.entur.lamassu.model.provider.FeedProvider;
import org.entur.lamassu.util.FeedUrlUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class DiscoveryFeedMapper implements FeedMapper<GBFS> {
    @Value("${org.entur.lamassu.baseUrl}")
    private String baseUrl;

    @Value("${org.entur.lamassu.targetLanguageCode:nb}")
    private String targetLanguageCode;

    @Value("${org.entur.lamassu.targetGbfsVersion:2.2}")
    private String targetGbfsVersion;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

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
        mapped.setVersion(GBFSGbfs.Version.fromValue(targetGbfsVersion));

        String sourceLanguageCode;
        if (source.getFeedsData().containsKey(feedProvider.getLanguage())) {
            sourceLanguageCode = feedProvider.getLanguage();
        } else {
            sourceLanguageCode = source.getFeedsData().keySet().iterator().next();
            logger.warn("Configured language code not found in discovery feed for provider {} - using {} instead", feedProvider, sourceLanguageCode);
        }

        var feeds = source.getFeedsData()
                .get(sourceLanguageCode)
                .getFeeds()
                .stream()
                .map(feed -> {
                    var mappedFeed = new GBFSFeed();
                    mappedFeed.setName(feed.getName());
                    mappedFeed.setUrl(FeedUrlUtil.mapFeedUrl(baseUrl, feed.getName(), feedProvider));
                    return mappedFeed;
                }).collect(Collectors.toList());

        if (feedProvider.getVehicleTypes() != null) {
            var vehicleTypesFeed = new GBFSFeed();
            vehicleTypesFeed.setName(GBFSFeedName.VehicleTypes);
            vehicleTypesFeed.setUrl(FeedUrlUtil.mapFeedUrl(baseUrl, GBFSFeedName.VehicleTypes, feedProvider));
            if (feeds.stream().anyMatch(f -> f.getName().equals(GBFSFeedName.VehicleTypes))) {
                feeds = feeds.stream().map(f -> {
                    if (f.getName().equals(GBFSFeedName.VehicleTypes)) {
                        return vehicleTypesFeed;
                    } else {
                        return f;
                    }
                }).collect(Collectors.toList());
            } else {
                feeds.add(vehicleTypesFeed);
            }
        }

        if (feedProvider.getPricingPlans() != null) {
            var pricingPlansFeed = new GBFSFeed();
            pricingPlansFeed.setName(GBFSFeedName.SystemPricingPlans);
            pricingPlansFeed.setUrl(FeedUrlUtil.mapFeedUrl(baseUrl, GBFSFeedName.SystemPricingPlans, feedProvider));
            if (feeds.stream().anyMatch(f -> f.getName().equals(GBFSFeedName.SystemPricingPlans))) {
                feeds = feeds.stream().map(f -> {
                    if (f.getName().equals(GBFSFeedName.SystemPricingPlans)) {
                        return pricingPlansFeed;
                    } else {
                        return f;
                    }
                }).collect(Collectors.toList());
            } else {
                feeds.add(pricingPlansFeed);
            }
        }

        mappedData.setFeeds(feeds);
        dataWrapper.put(targetLanguageCode, mappedData);
        mapped.setFeedsData(dataWrapper);
        return mapped;
    }
}
