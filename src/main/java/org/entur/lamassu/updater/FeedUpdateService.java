package org.entur.lamassu.updater;

import org.entur.lamassu.api.GBFSFeedApi;
import org.entur.lamassu.cache.GBFSFeedCache;
import org.entur.lamassu.config.feedprovider.FeedProviderConfig;
import org.entur.lamassu.mapper.DiscoveryFeedMapper;
import org.entur.lamassu.model.FeedProvider;
import org.entur.lamassu.model.gbfs.v2_1.GBFS;
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

    @Autowired
    private FeedUpdateScheduler feedUpdateScheduler;

    @Autowired
    private DiscoveryFeedMapper discoveryFeedMapper;

    public void fetchDiscoveryFeeds() {
        logger.debug("Fetching discovery feeds");
        feedProviderConfig.getProviders().parallelStream().forEach((FeedProvider feedProvider) ->
                feedUpdateScheduler.scheduleFetchDiscoveryFeed(feedProvider));
    }

    public void fetchDiscoveryFeed(FeedProvider feedProvider) {
        api.getDiscoveryFeed(feedProvider).subscribe(discovery -> {
            logger.debug("Fetched discovery feed {}", feedProvider.getUrl());
            var mappedFeed = discoveryFeedMapper.mapDiscoveryFeed(discovery, feedProvider);
            feedCache.update(GBFSFeedName.GBFS, feedProvider, mappedFeed);
            discovery.getData().get(feedProvider.getLanguage()).getFeeds().forEach(feedSource -> {
                logger.debug("Scheduling update for feed {} provider codespace: {}, city: {}, vehicleType: {}",
                        feedSource.getUrl(),
                        feedProvider.getCodespace(),
                        feedProvider.getCity(),
                        feedProvider.getVehicleType()
                );
                feedUpdateScheduler.scheduleFeedUpdate(feedProvider, discovery, feedSource.getName());
            });
        });
    }

    public void fetchFeed(FeedProvider feedProvider, GBFS discoveryFeed, GBFSFeedName feedName) {
        api.getFeed(discoveryFeed, feedName, feedProvider.getLanguage()).subscribe(feed -> {
            logger.debug("Fetched feed {} for provider codespace: {}, city: {}, vehicleType: {}",
                    feedName.toValue(),
                    feedProvider.getCodespace(),
                    feedProvider.getCity(),
                    feedProvider.getVehicleType()
            );
            feedCache.update(feedName, feedProvider, feed);
        });
    }
}
