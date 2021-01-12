package org.entur.lamassu.controller;

import org.entur.lamassu.cache.GBFSFeedCache;
import org.entur.lamassu.config.feedprovider.FeedProviderConfig;
import org.entur.lamassu.model.FeedProviderDiscovery;
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

    @Autowired
    ProviderDiscoveryService providerDiscoveryService;

    @Autowired
    GBFSFeedCache feedCache;

    @Autowired
    FeedProviderConfig feedProviderConfig;

    @GetMapping("/gbfs")
    public FeedProviderDiscovery getFeedProviderDiscovery() {
        return providerDiscoveryService.getFeedProviderDiscovery();
    }

    @GetMapping("/gbfs/{provider}/{feed}")
    public GBFSBase getGbfsFeedForProvider(@PathVariable String provider, @PathVariable String feed) {
        try {
            GBFSFeedName feedName = GBFSFeedName.valueOf(feed.toUpperCase());
            var response = feedProviderConfig.getProviders().stream()
                    .filter(fp -> fp.getName().equalsIgnoreCase(provider))
                    .findFirst()
                    .map(feedProvider -> feedCache.find(feedName, feedProvider))
                    .orElseThrow();

            if (response == null) {
                throw new NoSuchElementException();
            }

            return response;
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }
}
