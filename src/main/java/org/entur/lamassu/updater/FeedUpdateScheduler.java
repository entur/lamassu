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

    @Value("${org.entur.lamassu.baseUrl}")
    private String feedBaseUrl;

    private static final int NUM_CORES = Runtime.getRuntime().availableProcessors();

    private ForkJoinPool updaterThreadPool;

    @Autowired
    public FeedUpdateScheduler(FeedProviderConfig feedProviderConfig, GBFSFeedCacheV2 feedCache) {
        this.feedProviderConfig = feedProviderConfig;
        this.feedCache = feedCache;
    }
    public void start() {
        this.updaterThreadPool = new ForkJoinPool(NUM_CORES * 2);
        this.subscriptionManager = new GbfsSubscriptionManager(this.updaterThreadPool);
        this.updaterThreadPool.submit(new FeedUpdater(feedProviderConfig, feedCache, subscriptionManager, new DiscoveryFeedMapper(feedBaseUrl)));
    }

    public void update() {
        subscriptionManager.update();
    }

    public void stop() {
        updaterThreadPool.shutdown();
    }
}
