package org.entur.lamassu.api.impl;

import org.entur.lamassu.api.GBFSFeedApi;
import org.entur.lamassu.model.FeedProvider;
import org.entur.lamassu.model.gbfs.v2_1.GBFS;
import org.entur.lamassu.model.gbfs.v2_1.GBFSBase;
import org.entur.lamassu.model.gbfs.v2_1.GBFSFeed;
import org.entur.lamassu.model.gbfs.v2_1.GBFSFeedName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class GBFSFeedApiImpl implements GBFSFeedApi {
    private final WebClient webClient;

    public GBFSFeedApiImpl(@Autowired WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public Mono<GBFS> getDiscoveryFeed(FeedProvider feedProvider) {
        return get(feedProvider.getUrl(), GBFS.class);
    }

    @Override
    public Mono<? extends GBFSBase> getFeed(GBFS discoveryFeed, GBFSFeedName feedName, String language) {
        return discoveryFeed.getData().get(language).getFeeds().stream()
                .filter(feed -> feedName.equals(feed.getName()))
                .findFirst()
                .map(GBFSFeed::getUrl)
                .map(url -> get(url, getType(feedName)))
                .orElse(null);
    }

    private <T extends GBFSBase> Mono<T> get(String url, Class<T> type) {
        return webClient.get()
                .uri(url)
                .exchange()
                .flatMap(res -> res.bodyToMono(type));
    }

    private Class<? extends GBFSBase> getType(GBFSFeedName feedName) {
        switch (feedName) {
            case GBFS:
                return GBFS.class;
            default:
                throw new HttpServerErrorException(HttpStatus.NOT_IMPLEMENTED);
        }

    }
}
