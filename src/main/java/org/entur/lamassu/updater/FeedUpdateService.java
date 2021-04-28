package org.entur.lamassu.updater;

import org.entur.lamassu.api.GBFSFeedApi;
import org.entur.lamassu.cache.GBFSFeedCache;
import org.entur.lamassu.mapper.DiscoveryFeedMapper;
import org.entur.lamassu.model.discovery.FeedProvider;
import org.entur.lamassu.model.gbfs.v2_1.GBFS;
import org.entur.lamassu.model.gbfs.v2_1.GBFSBase;
import org.entur.lamassu.model.gbfs.v2_1.GBFSFeedName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

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

    public void update(FeedProvider feedProvider) {
        var discovery = fetchDiscoveryFeed(feedProvider).block();
        logger.debug("Fetched discovery feed  for provider {}", feedProvider.getSystemSlug());

        if (discovery == null) {
            logger.warn("Discovery response was null - unable to update feed for provider {}", feedProvider.getSystemSlug());
            return;
        }

        var mappedFeed = discoveryFeedMapper.mapDiscoveryFeed(discovery, feedProvider);
        feedCache.update(GBFSFeedName.GBFS, feedProvider, mappedFeed);
        discovery.getData().get(feedProvider.getLanguage()).getFeeds().stream()
                .sorted(this::sortFeeds)
                .forEach(feedSource -> {
                    var feed = fetchFeed(feedProvider, discovery, feedSource);
                    logger.debug("Fetched feed {} for provider {}", feedSource.getName(), feedProvider.getSystemSlug());
                    feedCache.update(feedSource.getName(), feedProvider, feed);
                });
    }

    private Mono<GBFS> fetchDiscoveryFeed(FeedProvider feedProvider) {
        logger.debug("Fetching discovery feed for provider {}", feedProvider.getSystemSlug());
        return api.getDiscoveryFeed(feedProvider);
    }

    private GBFSBase fetchFeed(FeedProvider feedProvider, GBFS discovery, GBFS.GBFSFeed feedSource) {
        logger.debug("Fetching feed {} for provider {}",
                feedSource.getUrl(),
                feedProvider.getSystemSlug()
        );
        var feedName = feedSource.getName();
        return api.getFeed(discovery, feedName, feedProvider.getLanguage()).block();
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
