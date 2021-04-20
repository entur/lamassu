package org.entur.lamassu.controller;

import org.entur.lamassu.cache.GBFSFeedCache;
import org.entur.lamassu.config.feedprovider.FeedProviderConfig;
import org.entur.lamassu.model.feedprovider.FeedProviderDiscovery;
import org.entur.lamassu.model.gbfs.v2_1.GBFSBase;
import org.entur.lamassu.model.gbfs.v2_1.GBFSFeedName;
import org.entur.lamassu.service.ProviderDiscoveryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.NoSuchElementException;

@RestController
public class GBFSFeedController {
    private final ProviderDiscoveryService providerDiscoveryService;
    private final GBFSFeedCache feedCache;
    private final FeedProviderConfig feedProviderConfig;

    @Autowired
    public GBFSFeedController(ProviderDiscoveryService providerDiscoveryService, GBFSFeedCache feedCache, FeedProviderConfig feedProviderConfig) {
        this.providerDiscoveryService = providerDiscoveryService;
        this.feedCache = feedCache;
        this.feedProviderConfig = feedProviderConfig;
    }

    @GetMapping("/gbfs")
    public FeedProviderDiscovery getFeedProviderDiscovery() {
        return providerDiscoveryService.getFeedProviderDiscovery();
    }

    @GetMapping(value = {"/gbfs/{provider}/{feed}", "/gbfs/{provider}/{feed}.json"})
    public GBFSBase getGbfsFeedForProvider(@PathVariable String provider, @PathVariable String feed) {
        try {
            var feedName = GBFSFeedName.valueOf(feed.toUpperCase());
            var feedProvider = feedProviderConfig.get(provider);
            return feedCache.find(feedName, feedProvider);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }
}
