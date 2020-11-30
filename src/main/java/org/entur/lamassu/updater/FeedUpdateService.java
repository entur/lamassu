package org.entur.lamassu.updater;

import org.entur.lamassu.api.GBFSFeedApi;
import org.entur.lamassu.cache.GBFSFeedCache;
import org.entur.lamassu.config.feedprovider.FeedProviderConfig;
import org.entur.lamassu.model.FeedProvider;
import org.entur.lamassu.model.gbfs.v2_1.GBFS;
import org.entur.lamassu.model.gbfs.v2_1.GBFSFeedName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class FeedUpdateService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private GBFSFeedApi api;

    @Autowired
    private FeedProviderConfig feedProviderConfig;

    @Autowired
    private GBFSFeedCache feedCache;

    @Autowired
    private FeedUpdateScheduler feedUpdateScheduler;

    public void fetchDiscoveryFeeds() {
        logger.info("Fetching discovery feeds");
        feedProviderConfig.getProviders().parallelStream().forEach((FeedProvider feedProvider) ->
                feedUpdateScheduler.scheduleFetchDiscoveryFeed(feedProvider));
    }

    public void fetchDiscoveryFeed(FeedProvider feedProvider) {
        api.getDiscoveryFeed(feedProvider).subscribe(discovery -> {
            logger.info("Fetched discovery feed {}", feedProvider.getUrl());

            // invoke mapper here before updating cache
            feedCache.update(GBFSFeedName.GBFS, feedProvider, discovery);
            discovery.getData().get(feedProvider.getLanguage()).getFeeds().forEach(feedSource -> {
                feedUpdateScheduler.scheduleFeedUpdate(feedProvider, discovery, feedSource.getName());
            });
        });
    }

    public void fetchFeed(FeedProvider feedProvider, GBFS discoveryFeed, GBFSFeedName feedName) {
        api.getFeed(discoveryFeed, feedName, feedProvider.getLanguage()).subscribe(feed -> {
            // invoke mapper here if applicable
            feedCache.update(feedName, feedProvider, feed);
        });
    }
}