package org.entur.lamassu.api;

import org.entur.lamassu.model.provider.FeedProvider;
import org.entur.lamassu.model.gbfs.v2_1.GBFS;
import org.entur.lamassu.model.gbfs.v2_1.GBFSBase;
import org.entur.lamassu.model.gbfs.v2_1.GBFSFeedName;
import reactor.core.publisher.Mono;

public interface GBFSFeedApi {
    Mono<GBFS> getDiscoveryFeed(FeedProvider feedProvider);
    Mono<? extends GBFSBase> getFeed(GBFS discoveryFeed, GBFSFeedName feedName, String language);
}
