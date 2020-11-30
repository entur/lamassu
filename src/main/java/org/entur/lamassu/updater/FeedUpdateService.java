package org.entur.lamassu.updater;

import org.entur.lamassu.api.GBFSFeedApi;
import org.entur.lamassu.cache.GBFSFeedCache;
import org.entur.lamassu.config.feedprovider.FeedProviderConfig;
import org.entur.lamassu.model.FeedProvider;
import org.entur.lamassu.model.gbfs.v2_1.GBFSFeedName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FeedUpdateService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private GBFSFeedApi api;

    @Autowired
    private FeedProviderConfig feedProviderConfig;

    @Autowired
    private GBFSFeedCache feedCache;

    public void update() {
        fetchDiscoveryFeeds();
    }

    private void fetchDiscoveryFeeds() {
        logger.info("Fetching discovery feeds");
        feedProviderConfig.getProviders().parallelStream().forEach((FeedProvider feedprovider) ->
            api.getDiscoveryFeed(feedprovider).subscribe(feed -> {
                logger.info("Fetched discovery feed {}", feedprovider.getUrl());
                feedCache.update(GBFSFeedName.GBFS, feedprovider.getCodespace(), feedprovider.getCity(), feed);
            })
        );
    }
}
