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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

@Component
public class FeedUpdateScheduler {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final FeedProviderConfig feedProviderConfig;
    private final GBFSFeedCacheV2 feedCache;

    private GbfsSubscriptionManager subscriptionManager;
    private final List<String> subscriptions = new ArrayList<>();

    @Value("${org.entur.lamassu.baseUrl}")
    private String feedBaseUrl;
    private DiscoveryFeedMapper discoveryFeedMapper;

    private static final int NUM_CORES = Runtime.getRuntime().availableProcessors();

    private ForkJoinPool updaterThreadPool;

    @Autowired
    public FeedUpdateScheduler(FeedProviderConfig feedProviderConfig, GBFSFeedCacheV2 feedCache) {
        this.feedProviderConfig = feedProviderConfig;
        this.feedCache = feedCache;
    }

    @PostConstruct
    public void init() {
        this.discoveryFeedMapper = new DiscoveryFeedMapper(feedBaseUrl);

    }

    public void start() {
        this.updaterThreadPool = new ForkJoinPool(NUM_CORES * 2);
        this.subscriptionManager = new GbfsSubscriptionManager(this.updaterThreadPool);

        this.updaterThreadPool.submit(() -> {
            feedProviderConfig.getProviders().parallelStream().forEach(feedProvider -> {
                var options = new GbfsSubscriptionOptions();
                options.setDiscoveryURI(URI.create(feedProvider.getUrl()));
                options.setLanguageCode(feedProvider.getLanguage());

                subscriptions.add(
                        subscriptionManager.subscribe(options, delivery -> updateFeedCaches(feedProvider, delivery))
                );
            });
        });
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

    public void update() {
        subscriptionManager.update();
    }

    public void stop() {
        subscriptions.forEach(subscriptionManager::unsubscribe);
        updaterThreadPool.shutdown();
    }
}
