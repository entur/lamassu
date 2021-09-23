package org.entur.lamassu.controller;

import org.entur.gbfs.v2_2.gbfs.GBFSFeedName;
import org.entur.lamassu.cache.GBFSFeedCacheV2;
import org.entur.lamassu.model.discovery.SystemDiscovery;
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
    private final GBFSFeedCacheV2 feedCache;
    private final FeedProviderService feedProviderService;

    @Autowired
    public GBFSFeedController(SystemDiscoveryService systemDiscoveryService, GBFSFeedCacheV2 feedCache, FeedProviderService feedProviderService) {
        this.systemDiscoveryService = systemDiscoveryService;
        this.feedCache = feedCache;
        this.feedProviderService = feedProviderService;
    }

    @GetMapping("/gbfs")
    public SystemDiscovery getFeedProviderDiscovery() {
        return systemDiscoveryService.getSystemDiscovery();
    }

    @GetMapping(value = {"/gbfs/{systemId}/{feed}", "/gbfs/{systemId}/{feed}.json"})
    public Object getGbfsFeedForProvider(@PathVariable String systemId, @PathVariable String feed) {
        try {
            var feedName = GBFSFeedName.fromValue(feed);
            var feedProvider = feedProviderService.getFeedProviderBySystemId(systemId);

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
