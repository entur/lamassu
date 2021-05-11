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
        fetchDiscoveryFeed(feedProvider);
    }

    private void fetchDiscoveryFeed(FeedProvider feedProvider) {
        logger.debug("Fetching discovery feed for provider {}", feedProvider.getSystemSlug());
        api.getDiscoveryFeed(feedProvider)
            .doOnSuccess(discovery -> onDiscoveryFeedFetchResult(discovery, feedProvider))
            .doOnError(e -> onDiscoveryFeedFetchError(e, feedProvider))
            .subscribe();
    }

    private void onDiscoveryFeedFetchError(Throwable e, FeedProvider feedProvider) {
        logger.warn("Caught exception while fetching discovery feed for provider={}", feedProvider, e);
    }

    public void onDiscoveryFeedFetchResult(GBFS discovery, FeedProvider feedProvider) {
        logger.debug("Fetched discovery feed  for provider {}", feedProvider.getSystemSlug());

        if (discovery == null) {
            logger.warn("Discovery response was null - unable to update feed for provider {}", feedProvider.getSystemSlug());
            return;
        }

        var mappedFeed = discoveryFeedMapper.mapDiscoveryFeed(discovery, feedProvider);

        if (mappedFeed == null) {
            logger.warn("Skipping update for provider due to missing discovery feed {}", feedProvider);
            return;
        }

        feedCache.update(GBFSFeedName.GBFS, feedProvider, mappedFeed);

        discovery.getData().get(feedProvider.getLanguage()).getFeeds().stream()
                .sorted(this::sortFeeds)
                .forEach(feedSource -> fetchFeed(feedProvider, discovery, feedSource));
    }

    private void fetchFeed(FeedProvider feedProvider, GBFS discovery, GBFS.GBFSFeed feedSource) {
        logger.debug("Fetching feed {} for provider {}",
                feedSource.getUrl(),
                feedProvider.getSystemSlug()
        );
        var feedName = feedSource.getName();
        api.getFeed(discovery, feedName, feedProvider.getLanguage())
                .doOnSuccess(feed -> handleFetchFeedSuccess(feed, feedSource, feedProvider))
                .doOnError(e -> handleFetchFeedError(e, feedSource, feedProvider))
                .subscribe();
    }

    private void handleFetchFeedSuccess(GBFSBase feed, GBFS.GBFSFeed feedSource, FeedProvider feedProvider) {
        logger.debug("Fetched feed {} for provider {}", feedSource.getName(), feedProvider.getSystemSlug());
        feedCache.update(feedSource.getName(), feedProvider, feed);
    }

    private void handleFetchFeedError(Throwable e, GBFS.GBFSFeed feedSource, FeedProvider feedProvider) {
        logger.warn("Caught exception while fetching feed={} for provider={}", feedSource, feedProvider, e);
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
