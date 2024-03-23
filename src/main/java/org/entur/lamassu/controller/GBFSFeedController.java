package org.entur.lamassu.controller;

import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;
import org.entur.gbfs.v2_3.gbfs.GBFSFeedName;
import org.entur.lamassu.cache.GBFSFeedCache;
import org.entur.lamassu.model.discovery.SystemDiscovery;
import org.entur.lamassu.service.FeedProviderService;
import org.entur.lamassu.service.SystemDiscoveryService;
import org.entur.lamassu.util.CacheUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class GBFSFeedController extends BaseGBFSFeedController {

  private final SystemDiscoveryService systemDiscoveryService;

  @Autowired
  public GBFSFeedController(
    SystemDiscoveryService systemDiscoveryService,
    GBFSFeedCache feedCache,
    FeedProviderService feedProviderService
  ) {
    super(feedCache, feedProviderService);
    this.systemDiscoveryService = systemDiscoveryService;
  }

  @GetMapping("/gbfs")
  public ResponseEntity<SystemDiscovery> getFeedProviderDiscovery() {
    var data = systemDiscoveryService.getSystemDiscovery();
    return ResponseEntity
      .ok()
      .cacheControl(CacheControl.maxAge(60, TimeUnit.MINUTES).cachePublic())
      .body(data);
  }

  @GetMapping(value = { "/gbfs/{systemId}/{feed}", "/gbfs/{systemId}/{feed}.json" })
  public ResponseEntity<Object> getGbfsFeedForProvider(
    @PathVariable String systemId,
    @PathVariable String feed
  ) {
    try {
      var feedName = GBFSFeedName.fromValue(feed);
      Object data = getFeed(systemId, feed);

      return ResponseEntity
        .ok()
        .cacheControl(
          CacheControl
            .maxAge(
              CacheUtil.getMaxAge(
                feedName.implementingClass(),
                data,
                systemId,
                feed,
                (int) Instant.now().getEpochSecond()
              ),
              TimeUnit.SECONDS
            )
            .cachePublic()
        )
        .lastModified(
          CacheUtil.getLastModified(feedName.implementingClass(), data, systemId, feed)
        )
        .body(data);
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    } catch (NoSuchElementException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
  }
}
