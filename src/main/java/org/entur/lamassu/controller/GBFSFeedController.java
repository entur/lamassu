package org.entur.lamassu.controller;

import org.entur.gbfs.v2_3.gbfs.GBFS;
import org.entur.gbfs.v2_3.gbfs.GBFSFeed;
import org.entur.gbfs.v2_3.gbfs.GBFSFeedName;
import org.entur.gbfs.v2_3.gbfs.GBFSFeeds;
import org.entur.lamassu.cache.GBFSFeedCache;
import org.entur.lamassu.model.discovery.System;
import org.entur.lamassu.model.discovery.SystemDiscovery;
import org.entur.lamassu.model.provider.FeedProvider;
import org.entur.lamassu.service.FeedProviderService;
import org.entur.lamassu.service.SystemDiscoveryService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RestController
public class GBFSFeedController {
    private final SystemDiscoveryService systemDiscoveryService;
    private final GBFSFeedCache feedCache;
    private final FeedProviderService feedProviderService;

    @Value("${org.entur.lamassu.baseUrl}")
    private String baseUrl;

    @Value("${org.entur.lamassu.internalLoadBalancer}")
    private String internalLoadBalancer;

    @Autowired
    public GBFSFeedController(SystemDiscoveryService systemDiscoveryService, GBFSFeedCache feedCache, FeedProviderService feedProviderService) {
        this.systemDiscoveryService = systemDiscoveryService;
        this.feedCache = feedCache;
        this.feedProviderService = feedProviderService;
    }

    @GetMapping("/gbfs")
    public ResponseEntity<SystemDiscovery> getFeedProviderDiscovery() {
        var data = systemDiscoveryService.getSystemDiscovery();
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(60, TimeUnit.MINUTES).cachePublic())
                .body(data);
    }

    @GetMapping("/gbfs-internal")
    public ResponseEntity<SystemDiscovery> getInternalFeedProviderDiscovery() {
        var data = systemDiscoveryService.getSystemDiscovery();
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(60, TimeUnit.MINUTES).cachePublic())
                .body(modifySystemDiscoveryUrls(data));
    }

    private SystemDiscovery modifySystemDiscoveryUrls(SystemDiscovery data) {
        var systemDiscovery = new SystemDiscovery();
        systemDiscovery.setSystems(
                data.getSystems().stream().map(s -> {
                    var system = new System();
                    system.setId(s.getId());
                    system.setUrl(
                            s.getUrl().replace(baseUrl + "/gbfs", internalLoadBalancer + "/gbfs-internal")
                    );
                    return system;
                }).collect(Collectors.toList())
        );
        return systemDiscovery;
    }

    @GetMapping(value = {"/gbfs/{systemId}/{feed}", "/gbfs/{systemId}/{feed}.json"})
    public ResponseEntity<Object> getGbfsFeedForProvider(@PathVariable String systemId, @PathVariable String feed) {
        try {
            var feedName = GBFSFeedName.fromValue(feed);
            Object data = getFeed(systemId, feed);

            return ResponseEntity.ok()
                    .cacheControl(CacheControl.maxAge(getMaxAge(feedName, data), TimeUnit.SECONDS).cachePublic())
                    .body(data);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping(value = {"/gbfs-internal/{systemId}/{feed}", "/gbfs-internal/{systemId}/{feed}.json"})
    public Object getInternalGbfsFeedForProvider(@PathVariable String systemId, @PathVariable String feed) {
        try {
            var feedName = GBFSFeedName.fromValue(feed);
            var feedProvider = feedProviderService.getFeedProviderBySystemId(systemId);
            Object data = getFeed(systemId, feed);
            if (feedName.equals(GBFSFeedName.GBFS)) {
                data = modifyDiscoveryUrls(feedProvider, (GBFS) data);
            }

            return ResponseEntity.ok()
                    .cacheControl(CacheControl.maxAge(getMaxAge(feedName, data), TimeUnit.SECONDS).cachePublic())
                    .body(data);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    private int getMaxAge(GBFSFeedName feedName, Object data) {
        int maxAge = 60;
        try {
            Integer lastUpdated = (Integer) feedName.implementingClass().getMethod("getLastUpdated").invoke(data);
            Integer ttl = (Integer) feedName.implementingClass().getMethod("getTtl").invoke(data);

            if (lastUpdated != null && ttl != null) {
                maxAge = getCurrentTimeSeconds() - lastUpdated + ttl;
            }

        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            // log something?
        }

        return maxAge;
    }

    private int getCurrentTimeSeconds() {
        return (int)(java.lang.System.currentTimeMillis() / 1000L);
    }

    @NotNull
    private Object getFeed(String systemId, String feed) {
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
    }

    private GBFS modifyDiscoveryUrls(FeedProvider feedProvider, GBFS data) {
        var gbfs = new GBFS();
        gbfs.setFeedsData(
                data.getFeedsData().values().stream().map(gbfsFeeds -> {
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
        return gbfs;
    }
}
