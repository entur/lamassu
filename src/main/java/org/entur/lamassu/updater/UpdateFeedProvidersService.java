package org.entur.lamassu.updater;

import org.entur.lamassu.cache.jcache.GBFSFeedCacheJCache;
import org.entur.lamassu.config.FeedProviderConfig;
import org.entur.lamassu.model.FeedProvider;
import org.entur.lamassu.model.gbfs.v2_1.GBFS;
import org.entur.lamassu.model.gbfs.v2_1.GBFSFeedName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class UpdateFeedProvidersService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private WebClient webClient;

    @Autowired
    private FeedProviderConfig feedProviderConfig;

    @Autowired
    private GBFSFeedCacheJCache feedCache;

    public void update() {
        fetchDiscoveryFeeds();
    }

    private void fetchDiscoveryFeeds() {
        logger.info("Fetching discovery feeds");
        feedProviderConfig.getProviders().parallelStream().forEach((FeedProvider feedprovider) ->
                webClient.get()
                    .uri(feedprovider.getUrl())
                    .exchange()
                    .flatMap(res -> res.bodyToMono(GBFS.class))
                    .doOnError(Throwable::printStackTrace)
                    .subscribe(feed -> {
                        logger.info("Fetched discovery feed {}", feedprovider.getUrl());
                        feedCache.update(GBFSFeedName.GBFS, feedprovider.getCodespace(), feedprovider.getCity(), feed);
                    }));
    }
}
