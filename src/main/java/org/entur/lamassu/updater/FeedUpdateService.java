package org.entur.lamassu.updater;

import org.entur.lamassu.api.GBFSFeedApi;
import org.entur.lamassu.cache.GBFSFeedCache;
import org.entur.lamassu.mapper.DiscoveryFeedMapper;
import org.entur.lamassu.model.feedprovider.FeedProvider;
import org.entur.lamassu.model.gbfs.v2_1.GBFS;
import org.entur.lamassu.model.gbfs.v2_1.GBFSFeedName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FeedUpdateService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final GBFSFeedApi api;
    private final GBFSFeedCache feedCache;
    private final DiscoveryFeedMapper discoveryFeedMapper;

    @Autowired
    public FeedUpdateService(GBFSFeedApi api, GBFSFeedCache feedCache, DiscoveryFeedMapper discoveryFeedMapper) {
        this.api = api;
        this.feedCache = feedCache;
        this.discoveryFeedMapper = discoveryFeedMapper;
    }

    public void fetchDiscoveryFeed(FeedProvider feedProvider) {
        api.getDiscoveryFeed(feedProvider).subscribe(discovery -> {
            logger.info("Fetched discovery feed {}", feedProvider.getUrl());
            var mappedFeed = discoveryFeedMapper.mapDiscoveryFeed(discovery, feedProvider);
            feedCache.update(GBFSFeedName.GBFS, feedProvider, mappedFeed);
            discovery.getData().get(feedProvider.getLanguage()).getFeeds().stream()
                    .sorted(this::sortFeeds)
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

    private static final List<GBFSFeedName> feedPriority = List.of(
            GBFSFeedName.STATION_INFORMATION,
            GBFSFeedName.STATION_STATUS,
            GBFSFeedName.FREE_BIKE_STATUS
    );

    private int sortFeeds(GBFS.GBFSFeed a, GBFS.GBFSFeed b) {
        return Integer.compare(feedPriority.indexOf(a.getName()), feedPriority.indexOf(b.getName()));
    }
}
