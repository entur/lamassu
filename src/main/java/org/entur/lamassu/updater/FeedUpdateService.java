package org.entur.lamassu.updater;

import org.entur.lamassu.api.GBFSFeedApi;
import org.entur.lamassu.cache.GBFSFeedCache;
import org.entur.lamassu.config.feedprovider.FeedProviderConfig;
import org.entur.lamassu.mapper.DiscoveryFeedMapper;
import org.entur.lamassu.model.feedprovider.FeedProvider;
import org.entur.lamassu.model.gbfs.v2_1.GBFS;
import org.entur.lamassu.model.gbfs.v2_1.GBFSFeedName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FeedUpdateService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final GBFSFeedApi api;
    private final FeedProviderConfig feedProviderConfig;
    private final GBFSFeedCache feedCache;
    private final DiscoveryFeedMapper discoveryFeedMapper;

    @Autowired
    public FeedUpdateService(GBFSFeedApi api, FeedProviderConfig feedProviderConfig, GBFSFeedCache feedCache, DiscoveryFeedMapper discoveryFeedMapper) {
        this.api = api;
        this.feedProviderConfig = feedProviderConfig;
        this.feedCache = feedCache;
        this.discoveryFeedMapper = discoveryFeedMapper;
    }

    public void fetchDiscoveryFeeds() {
        logger.debug("Fetching discovery feeds");
        feedProviderConfig.getProviders().forEach(this::fetchDiscoveryFeed);
    }

    private void fetchDiscoveryFeed(FeedProvider feedProvider) {
        api.getDiscoveryFeed(feedProvider).subscribe(discovery -> {
            logger.info("Fetched discovery feed {}", feedProvider.getUrl());
            var mappedFeed = discoveryFeedMapper.mapDiscoveryFeed(discovery, feedProvider);
            feedCache.update(GBFSFeedName.GBFS, feedProvider, mappedFeed);
            discovery.getData().get(feedProvider.getLanguage()).getFeeds().stream()
                    .sorted(this::sortFreeBikeStatusLast)
                    .forEach(feedSource -> fetchFeed(feedProvider, discovery, feedSource));
        });
    }

    private void fetchFeed(FeedProvider feedProvider, GBFS discovery, GBFS.GBFSFeed feedSource) {
        logger.info("Updating feed {} provider codespace: {}, city: {}, vehicleType: {}",
                feedSource.getUrl(),
                feedProvider.getCodespace(),
                feedProvider.getCity(),
                feedProvider.getVehicleType()
        );
        var feedName = feedSource.getName();
        api.getFeed(discovery, feedName, feedProvider.getLanguage()).subscribe(feed -> {
            logger.info("Fetched feed {} for provider codespace: {}, city: {}, vehicleType: {}",
                    feedName.toValue(),
                    feedProvider.getCodespace(),
                    feedProvider.getCity(),
                    feedProvider.getVehicleType()
            );
            feedCache.update(feedName, feedProvider, feed);
        });
    }

    // When feeds are fetched sequentially, ensure FREE_BIKE_STATUS is fetched last, in order
    // to ensure that all feeds are up-to-date when populating vehicle cache
    private int sortFreeBikeStatusLast(GBFS.GBFSFeed a, GBFS.GBFSFeed b) {
        if (a.getName().equals(GBFSFeedName.FREE_BIKE_STATUS)) {
            return 1;
        } else if (b.getName().equals(GBFSFeedName.FREE_BIKE_STATUS)) {
            return 1;
        } else {
            return -1;
        }
    }
}
