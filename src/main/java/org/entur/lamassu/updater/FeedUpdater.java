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
import org.entur.gbfs.v2_2.gbfs.GBFS;
import org.entur.gbfs.v2_2.gbfs.GBFSFeedName;
import org.entur.gbfs.v2_2.system_alerts.GBFSSystemAlerts;
import org.entur.lamassu.cache.GBFSFeedCacheV2;
import org.entur.lamassu.config.feedprovider.FeedProviderConfig;
import org.entur.lamassu.mapper.feedmapper.FeedMapper;
import org.entur.lamassu.model.provider.FeedProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.concurrent.ForkJoinPool;

@Component
public class FeedUpdater {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final FeedProviderConfig feedProviderConfig;
    private final GBFSFeedCacheV2 feedCache;
    private final FeedMapper<GBFS> discoveryFeedMapper;
    private final FeedMapper<GBFSSystemAlerts> systemAlertsFeedMapper;

    private static final int NUM_CORES = Runtime.getRuntime().availableProcessors();

    private GbfsSubscriptionManager subscriptionManager;
    private ForkJoinPool updaterThreadPool;

    @Autowired
    public FeedUpdater(
            FeedProviderConfig feedProviderConfig,
            GBFSFeedCacheV2 feedCache,
            FeedMapper<GBFS> discoveryFeedMapper,
            FeedMapper<GBFSSystemAlerts> systemAlertsFeedMapper

    ) {
        this.feedProviderConfig = feedProviderConfig;
        this.feedCache = feedCache;
        this.discoveryFeedMapper = discoveryFeedMapper;
        this.systemAlertsFeedMapper = systemAlertsFeedMapper;
    }

    public void start() {
        updaterThreadPool = new ForkJoinPool(NUM_CORES * 2);
        subscriptionManager = new GbfsSubscriptionManager(updaterThreadPool);
        updaterThreadPool.submit(this::createSubscriptions);
    }

    public void update() {
        subscriptionManager.update();
    }

    public void stop() {
        updaterThreadPool.shutdown();
    }

    private void createSubscriptions() {
        feedProviderConfig.getProviders().parallelStream().forEach(this::createSubscription);
    }

    private void createSubscription(FeedProvider feedProvider) {
        var options = new GbfsSubscriptionOptions();
        options.setDiscoveryURI(URI.create(feedProvider.getUrl()));
        options.setLanguageCode(feedProvider.getLanguage());
        subscriptionManager.subscribe(options, delivery -> updateFeedCaches(feedProvider, delivery));
    }

    private void updateFeedCaches(FeedProvider feedProvider, GbfsDelivery delivery) {
        updateFeedCache(feedProvider, GBFSFeedName.GBFS, discoveryFeedMapper.map(delivery.getDiscovery(), feedProvider));
        updateFeedCache(feedProvider, GBFSFeedName.GBFSVersions, delivery.getVersion());
        updateFeedCache(feedProvider, GBFSFeedName.SystemInformation,delivery.getSystemInformation());
        updateFeedCache(feedProvider, GBFSFeedName.SystemAlerts, systemAlertsFeedMapper.map(delivery.getSystemAlerts(), feedProvider));
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
