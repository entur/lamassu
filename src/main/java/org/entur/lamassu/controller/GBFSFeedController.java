package org.entur.lamassu.controller;

import org.entur.lamassu.cache.GBFSFeedCache;
import org.entur.lamassu.model.discovery.SystemDiscovery;
import org.entur.lamassu.model.gbfs.v2_1.GBFSBase;
import org.entur.lamassu.model.gbfs.v2_1.GBFSFeedName;
import org.entur.lamassu.service.FeedProviderService;
import org.entur.lamassu.service.SystemDiscoveryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.NoSuchElementException;

@RestController
public class GBFSFeedController {
    private final SystemDiscoveryService systemDiscoveryService;
    private final GBFSFeedCache feedCache;
    private final FeedProviderService feedProviderService;

    @Autowired
    public GBFSFeedController(SystemDiscoveryService systemDiscoveryService, GBFSFeedCache feedCache, FeedProviderService feedProviderService) {
        this.systemDiscoveryService = systemDiscoveryService;
        this.feedCache = feedCache;
        this.feedProviderService = feedProviderService;
    }

    @GetMapping("/gbfs")
    public SystemDiscovery getFeedProviderDiscovery() {
        return systemDiscoveryService.getSystemDiscovery();
    }

    @GetMapping(value = {"/gbfs/{provider}/{feed}", "/gbfs/{provider}/{feed}.json"})
    public GBFSBase getGbfsFeedForProvider(@PathVariable String provider, @PathVariable String feed) {
        try {
            var feedName = GBFSFeedName.valueOf(feed.toUpperCase());
            var feedProvider = feedProviderService.getFeedProviderBySystemSlug(provider);

            if (feedProvider == null) {
                throw new NoSuchElementException();
            }

            var data = feedCache.find(feedName, feedProvider);

            if (data == null) {
                throw new NoSuchElementException();
            }

            return data;
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }
}
