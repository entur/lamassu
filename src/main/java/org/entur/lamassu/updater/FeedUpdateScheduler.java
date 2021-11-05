package org.entur.lamassu.updater;

import org.entur.gbfs.GbfsSubscriptionManager;
import org.entur.lamassu.cache.GBFSFeedCacheV2;
import org.entur.lamassu.config.feedprovider.FeedProviderConfig;
import org.entur.lamassu.mapper.feedmapper.DiscoveryFeedMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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
