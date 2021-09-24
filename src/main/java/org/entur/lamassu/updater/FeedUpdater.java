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

package org.entur.lamassu.updater;

import org.entur.gbfs.GbfsDelivery;
import org.entur.gbfs.GbfsSubscriptionManager;
import org.entur.gbfs.GbfsSubscriptionOptions;
import org.entur.gbfs.v2_2.gbfs.GBFSFeedName;
import org.entur.lamassu.cache.GBFSFeedCacheV2;
import org.entur.lamassu.config.feedprovider.FeedProviderConfig;
import org.entur.lamassu.mapper.DiscoveryFeedMapper;
import org.entur.lamassu.model.provider.FeedProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

public class FeedUpdater implements Runnable {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final FeedProviderConfig feedProviderConfig;
    private final GBFSFeedCacheV2 feedCache;
    private final GbfsSubscriptionManager subscriptionManager;
    private final DiscoveryFeedMapper discoveryFeedMapper;

    public FeedUpdater(
            FeedProviderConfig feedProviderConfig,
            GBFSFeedCacheV2 feedCache,
            GbfsSubscriptionManager subscriptionManager,
            DiscoveryFeedMapper discoveryFeedMapper
    ) {
        this.feedProviderConfig = feedProviderConfig;
        this.feedCache = feedCache;
        this.subscriptionManager = subscriptionManager;
        this.discoveryFeedMapper = discoveryFeedMapper;
    }

    @Override
    public void run() {
        feedProviderConfig.getProviders().parallelStream().forEach(this::createSubscription);
    }

    private void createSubscription(FeedProvider feedProvider) {
        var options = new GbfsSubscriptionOptions();
        options.setDiscoveryURI(URI.create(feedProvider.getUrl()));
        options.setLanguageCode(feedProvider.getLanguage());
        subscriptionManager.subscribe(options, delivery -> updateFeedCaches(feedProvider, delivery));
    }

    private void updateFeedCaches(FeedProvider feedProvider, GbfsDelivery delivery) {
        updateFeedCache(feedProvider, GBFSFeedName.GBFS, discoveryFeedMapper.mapDiscoveryFeed(delivery.getDiscovery(), feedProvider));
        updateFeedCache(feedProvider, GBFSFeedName.GBFSVersions, delivery.getVersion());
        updateFeedCache(feedProvider, GBFSFeedName.SystemInformation,delivery.getSystemInformation());
        updateFeedCache(feedProvider, GBFSFeedName.SystemAlerts, delivery.getSystemAlerts());
        updateFeedCache(feedProvider, GBFSFeedName.SystemCalendar, delivery.getSystemCalendar());
        updateFeedCache(feedProvider, GBFSFeedName.SystemRegions, delivery.getSystemRegions());
        updateFeedCache(feedProvider, GBFSFeedName.SystemPricingPlans, delivery.getSystemPricingPlans());
        updateFeedCache(feedProvider, GBFSFeedName.SystemHours, delivery.getSystemHours());
        updateFeedCache(feedProvider, GBFSFeedName.VehicleTypes, delivery.getVehicleTypes());
        updateFeedCache(feedProvider, GBFSFeedName.GeofencingZones, delivery.getGeofencingZones());
        updateFeedCache(feedProvider, GBFSFeedName.StationInformation, delivery.getStationInformation());
        updateFeedCache(feedProvider, GBFSFeedName.StationStatus, delivery.getStationStatus());
        updateFeedCache(feedProvider, GBFSFeedName.FreeBikeStatus, delivery.getFreeBikeStatus());
    }

    private void updateFeedCache(FeedProvider feedProvider, GBFSFeedName feedName, Object feed) {
        if (feed != null) {
            logger.info("updating feed {} for provider {}", feedName, feedProvider.getSystemId());
            logger.trace("updating feed {} for provider {} data {}", feedName, feedProvider.getSystemId(), feed);
            feedCache.update(feedName, feedProvider, feed);
        }
    }


}
