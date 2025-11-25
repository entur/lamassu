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

import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;
import org.entur.lamassu.cache.GBFSV3FeedCache;
import org.entur.lamassu.model.provider.FeedProvider;
import org.entur.lamassu.service.FeedProviderService;
import org.entur.lamassu.service.SystemDiscoveryService;
import org.entur.lamassu.util.CacheUtil;
import org.jetbrains.annotations.NotNull;
import org.mobilitydata.gbfs.v3_0.gbfs.GBFSFeed;
import org.mobilitydata.gbfs.v3_0.gbfs.GBFSGbfs;
import org.mobilitydata.gbfs.v3_0.manifest.GBFSManifest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping({ "/gbfs/v3" })
public class GBFSV3FeedController {

  @Value("${org.entur.lamassu.gbfs.cache-control.minimum-ttl:0}")
  private int cacheControlMinimumTtl;

  private final SystemDiscoveryService systemDiscoveryService;
  private final FeedProviderService feedProviderService;
  private final GBFSV3FeedCache v3FeedCache;

  @Autowired
  public GBFSV3FeedController(
    SystemDiscoveryService systemDiscoveryService,
    GBFSV3FeedCache v3FeedCache,
    FeedProviderService feedProviderService
  ) {
    this.v3FeedCache = v3FeedCache;
    this.systemDiscoveryService = systemDiscoveryService;
    this.feedProviderService = feedProviderService;
  }

  @GetMapping("/manifest.json")
  public ResponseEntity<GBFSManifest> getV3Manifest() {
    var manifest = systemDiscoveryService.getGBFSManifest();

    return ResponseEntity
      .ok()
      .cacheControl(CacheControl.maxAge(3600, TimeUnit.MINUTES).cachePublic())
      .body(manifest);
  }

  @GetMapping(value = { "/{systemId}/{feed}", "/{systemId}/{feed}.json" })
  public ResponseEntity<Object> getV3Feed(
    @PathVariable String systemId,
    @PathVariable String feed,
    @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch
  ) {
    try {
      var feedName = GBFSFeed.Name.fromValue(feed);

      var data = getFeed(systemId, feed);
      String etag = CacheUtil.generateETag(data, systemId, feed);

      if (ifNoneMatch != null && ifNoneMatch.equals(etag)) {
        return ResponseEntity
          .status(HttpStatus.NOT_MODIFIED)
          .cacheControl(
            CacheControl
              .maxAge(
                CacheUtil.getMaxAge(
                  org.mobilitydata.gbfs.v3_0.gbfs.GBFSFeedName.implementingClass(
                    feedName
                  ),
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
                org.mobilitydata.gbfs.v3_0.gbfs.GBFSFeedName.implementingClass(feedName),
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
          CacheUtil.getLastModified(
            org.mobilitydata.gbfs.v3_0.gbfs.GBFSFeedName.implementingClass(feedName),
            data,
            systemId,
            feed
          )
        )
        .eTag(etag)
        .body(data);
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    } catch (NoSuchElementException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
  }

  @NotNull
  protected Object getFeed(String systemId, String feed) {
    var feedName = GBFSFeed.Name.fromValue(feed);
    var feedProvider = feedProviderService.getFeedProviderBySystemId(systemId);

    if (feedProvider == null) {
      throw new NoSuchElementException();
    }

    var data = v3FeedCache.find(feedName, feedProvider);

    if (data == null) {
      throwsIfFeedCouldOrShouldExist(feedName, feedProvider);
      throw new NoSuchElementException();
    }
    return data;
  }

  /*
    Throws an UpstreamFeedNotYetAvailableException, if either the discoveryFile (gbf file) is not yet cached,
    the requested feed is published in the discovery file, or the discovery file is malformed.
   */
  protected void throwsIfFeedCouldOrShouldExist(
    GBFSFeed.Name feedName,
    FeedProvider feedProvider
  ) {
    try {
      GBFSGbfs discoveryFile = (GBFSGbfs) v3FeedCache.find(
        GBFSFeed.Name.GBFS,
        feedProvider
      );
      if (
        discoveryFile == null ||
        discoveryFile
          .getData()
          .getFeeds()
          .stream()
          .map(GBFSFeed::getName)
          .anyMatch(name -> name.equals(feedName))
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
