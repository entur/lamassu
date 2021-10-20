package org.entur.lamassu.controller;

import org.entur.gbfs.v2_2.gbfs.GBFS;
import org.entur.gbfs.v2_2.gbfs.GBFSFeed;
import org.entur.gbfs.v2_2.gbfs.GBFSFeedName;
import org.entur.gbfs.v2_2.gbfs.GBFSFeeds;
import org.entur.lamassu.cache.GBFSFeedCacheV2;
import org.entur.lamassu.model.discovery.System;
import org.entur.lamassu.model.discovery.SystemDiscovery;
import org.entur.lamassu.model.provider.FeedProvider;
import org.entur.lamassu.service.FeedProviderService;
import org.entur.lamassu.service.SystemDiscoveryService;
import org.entur.lamassu.util.FeedUrlUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@RestController
public class GBFSFeedController {
    private final SystemDiscoveryService systemDiscoveryService;
    private final GBFSFeedCacheV2 feedCache;
    private final FeedProviderService feedProviderService;

    @Value("${org.entur.lamassu.baseUrl}")
    private String baseUrl;

    @Value("${org.entur.lamassu.internalLoadBalancer}")
    private String internalLoadBalancer;

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

    @GetMapping("/gbfs-internal")
    public SystemDiscovery getInternalFeedProviderDiscovery() {
        var data = getFeedProviderDiscovery();
        modifySystemDiscoveryUrls(data);
        return data;
    }

    private void modifySystemDiscoveryUrls(SystemDiscovery data) {
        data.setSystems(
                data.getSystems().stream().map(s -> {
                    var system = new System();
                    system.setId(s.getId());
                    system.setUrl(
                            s.getUrl().replace(baseUrl + "/gbfs", internalLoadBalancer + "/gbfs-internal")
                    );
                    return system;
                }).collect(Collectors.toList())
        );
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

    @GetMapping(value = {"/gbfs-internal/{systemId}/{feed}", "/gbfs/{systemId}/{feed}.json"})
    public Object getInternalGbfsFeedForProvider(@PathVariable String systemId, @PathVariable String feed) {
        var feedName = GBFSFeedName.fromValue(feed);
        var feedProvider = feedProviderService.getFeedProviderBySystemId(systemId);
        var data = getGbfsFeedForProvider(systemId, feed);
        if (feedName.equals(GBFSFeedName.GBFS)) {
            modifyDiscoveryUrls(feedProvider, (GBFS) data);
        }
        return data;
    }

    private void modifyDiscoveryUrls(FeedProvider feedProvider, GBFS data) {
        var gbfs = data;
        gbfs.setFeedsData(
                gbfs.getFeedsData().entrySet().stream().map(e -> {
                    var gbfsFeeds = e.getValue();
                    var mappedGbfsFeeds = new GBFSFeeds();
                    mappedGbfsFeeds.setFeeds(
                            gbfsFeeds.getFeeds().stream().map(f -> {
                                var gbfsFeed = new GBFSFeed();
                                gbfsFeed.setName(f.getName());
                                gbfsFeed.setUrl(
                                        URI.create(f.getUrl().toString().replace(baseUrl + "/gbfs", internalLoadBalancer + "/gbfs-internal"))
                                );
                                return gbfsFeed;
                            }).collect(Collectors.toList())
                    );
                    return mappedGbfsFeeds;
                }).collect(Collectors.toMap(e -> feedProvider.getLanguage(), e -> e))
        );
    }
}
