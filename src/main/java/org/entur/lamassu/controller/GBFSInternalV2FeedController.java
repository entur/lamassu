/*
 *
 *
 *  * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 *  * the European Commission - subsequent versions of the EUPL (the "Licence");
 *  * You may not use this work except in compliance with the Licence.
 *  * You may obtain a copy of the Licence at:
 *  *
 *  *   https://joinup.ec.europa.eu/software/page/eupl
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the Licence is distributed on an "AS IS" basis,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the Licence for the specific language governing permissions and
 *  * limitations under the Licence.
 *
 */

package org.entur.lamassu.controller;

import java.net.URI;
import java.time.Instant;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.entur.lamassu.cache.GBFSV2FeedCache;
import org.entur.lamassu.model.discovery.System;
import org.entur.lamassu.model.discovery.SystemDiscovery;
import org.entur.lamassu.model.provider.FeedProvider;
import org.entur.lamassu.service.FeedProviderService;
import org.entur.lamassu.service.SystemDiscoveryService;
import org.entur.lamassu.util.CacheUtil;
import org.jetbrains.annotations.NotNull;
import org.mobilitydata.gbfs.v2_3.gbfs.GBFS;
import org.mobilitydata.gbfs.v2_3.gbfs.GBFSFeed;
import org.mobilitydata.gbfs.v2_3.gbfs.GBFSFeedName;
import org.mobilitydata.gbfs.v2_3.gbfs.GBFSFeeds;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * This controller has endpoints that can be configured with an alternative
 * base url, e.g. for behind-proxy access.
 *
 * @deprecated The URL should instead be dynamic based on Host header or similar
 *              see {@link https://github.com/entur/lamassu/issues/263}
 */
@RestController
public class GBFSInternalV2FeedController {

  @Value("${org.entur.lamassu.gbfs.cache-control.minimum-ttl:0}")
  private int cacheControlMinimumTtl;

  private final SystemDiscoveryService systemDiscoveryService;

  private final FeedProviderService feedProviderService;

  private final GBFSV2FeedCache feedCache;

  @Value("${org.entur.lamassu.baseUrl}")
  private String baseUrl;

  @Value("${org.entur.lamassu.internalLoadBalancer}")
  private String internalLoadBalancer;

  @Autowired
  public GBFSInternalV2FeedController(
    SystemDiscoveryService systemDiscoveryService,
    GBFSV2FeedCache feedCache,
    FeedProviderService feedProviderService
  ) {
    this.systemDiscoveryService = systemDiscoveryService;
    this.feedCache = feedCache;
    this.feedProviderService = feedProviderService;
  }

  @GetMapping("/gbfs-internal")
  public ResponseEntity<SystemDiscovery> getInternalFeedProviderDiscovery() {
    var data = systemDiscoveryService.getSystemDiscovery();
    return ResponseEntity
      .ok()
      .cacheControl(CacheControl.maxAge(60, TimeUnit.MINUTES).cachePublic())
      .body(modifySystemDiscoveryUrls(data));
  }

  @GetMapping(
    value = {
      "/gbfs-internal/{systemId}/{feed}", "/gbfs-internal/{systemId}/{feed}.json",
    }
  )
  public Object getInternalGbfsFeedForProvider(
    @PathVariable String systemId,
    @PathVariable String feed,
    @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch
  ) {
    try {
      var feedName = GBFSFeedName.fromValue(feed);
      var feedProvider = feedProviderService.getFeedProviderBySystemId(systemId);
      Object data = getFeed(systemId, feed);
      if (feedName.equals(GBFSFeedName.GBFS)) {
        data = modifyDiscoveryUrls(feedProvider, (GBFS) data);
      }

      String etag = CacheUtil.generateETag(data, systemId, feed);

      if (ifNoneMatch != null && ifNoneMatch.equals(etag)) {
        return ResponseEntity
          .status(HttpStatus.NOT_MODIFIED)
          .cacheControl(
            CacheControl
              .maxAge(
                CacheUtil.getMaxAge(
                  feedName.implementingClass(),
                  data,
                  systemId,
                  feed,
                  (int) Instant.now().getEpochSecond(),
                  cacheControlMinimumTtl
                ),
                TimeUnit.SECONDS
              )
              .cachePublic()
          )
          .eTag(etag)
          .build();
      }

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
                (int) Instant.now().getEpochSecond(),
                cacheControlMinimumTtl
              ),
              TimeUnit.SECONDS
            )
            .cachePublic()
        )
        .lastModified(
          CacheUtil.getLastModified(feedName.implementingClass(), data, systemId, feed)
        )
        .eTag(etag)
        .body(data);
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    } catch (NoSuchElementException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
  }

  private SystemDiscovery modifySystemDiscoveryUrls(SystemDiscovery data) {
    var systemDiscovery = new SystemDiscovery();
    systemDiscovery.setSystems(
      data
        .getSystems()
        .stream()
        .map(s -> {
          var system = new System();
          system.setId(s.getId());
          system.setUrl(
            s
              .getUrl()
              .replace(baseUrl + "/gbfs/v2", internalLoadBalancer + "/gbfs-internal")
          );
          return system;
        })
        .collect(Collectors.toList())
    );
    return systemDiscovery;
  }

  private GBFS modifyDiscoveryUrls(FeedProvider feedProvider, GBFS data) {
    var gbfs = new GBFS();
    gbfs.setLastUpdated(data.getLastUpdated());
    gbfs.setTtl(data.getTtl());
    gbfs.setFeedsData(
      data
        .getFeedsData()
        .values()
        .stream()
        .map(gbfsFeeds -> {
          var mappedGbfsFeeds = new GBFSFeeds();
          mappedGbfsFeeds.setFeeds(
            gbfsFeeds
              .getFeeds()
              .stream()
              .map(f -> {
                var gbfsFeed = new GBFSFeed();
                gbfsFeed.setName(f.getName());
                gbfsFeed.setUrl(
                  URI.create(
                    f
                      .getUrl()
                      .toString()
                      .replace(
                        baseUrl + "/gbfs/v2",
                        internalLoadBalancer + "/gbfs-internal"
                      )
                  )
                );
                return gbfsFeed;
              })
              .collect(Collectors.toList())
          );
          return mappedGbfsFeeds;
        })
        .collect(Collectors.toMap(e -> feedProvider.getLanguage(), e -> e))
    );
    return gbfs;
  }

  @NotNull
  protected Object getFeed(String systemId, String feed) {
    var feedName = GBFSFeedName.fromValue(feed);
    var feedProvider = feedProviderService.getFeedProviderBySystemId(systemId);

    if (feedProvider == null) {
      throw new NoSuchElementException();
    }

    var data = feedCache.find(feedName, feedProvider);

    if (data == null) {
      throwsIfFeedCouldOrShouldExist(feedName, feedProvider);
      throw new NoSuchElementException();
    }
    return data;
  }

  /*
    Throws an UpstreamFeedNotYetAvailableException, if the feed provider is *enabled* AND
    either the discoveryFile (gbf file) is not yet cached, the requested feed is published
    in the discovery file, or the discovery file is malformed.
     */
  protected void throwsIfFeedCouldOrShouldExist(
    GBFSFeedName feedName,
    FeedProvider feedProvider
  ) {
    try {
      GBFS discoveryFile = feedCache.find(GBFSFeedName.GBFS, feedProvider);
      if (
        feedProvider.getEnabled() &&
        (
          discoveryFile == null ||
          discoveryFile
            .getFeedsData()
            .values()
            .stream()
            .map(GBFSFeeds::getFeeds)
            .flatMap(Collection::stream)
            .map(GBFSFeed::getName)
            .anyMatch(name -> name.equals(feedName))
        )
      ) {
        throw new UpstreamFeedNotYetAvailableException();
      }
    } catch (NullPointerException e) {
      // in case the gbfs is malformed, e.g. no languages are defined, or no feeds,
      // this is an upstream error and the requested feed might exist
      throw new UpstreamFeedNotYetAvailableException();
    }
  }
}
