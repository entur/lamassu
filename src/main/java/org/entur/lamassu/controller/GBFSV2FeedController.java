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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import org.entur.gbfs.v2_3.gbfs.GBFS;
import org.entur.gbfs.v2_3.gbfs.GBFSFeed;
import org.entur.gbfs.v2_3.gbfs.GBFSFeedName;
import org.entur.gbfs.v2_3.gbfs.GBFSFeeds;
import org.entur.lamassu.cache.GBFSV2FeedCache;
import org.entur.lamassu.model.discovery.SystemDiscovery;
import org.entur.lamassu.service.FeedProviderService;
import org.entur.lamassu.service.SystemDiscoveryService;
import org.entur.lamassu.util.CacheUtil;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class GBFSV2FeedController {

  private final SystemDiscoveryService systemDiscoveryService;

  private final FeedProviderService feedProviderService;

  private final GBFSV2FeedCache feedCache;

  @Autowired
  public GBFSV2FeedController(
    SystemDiscoveryService systemDiscoveryService,
    GBFSV2FeedCache feedCache,
    FeedProviderService feedProviderService
  ) {
    this.systemDiscoveryService = systemDiscoveryService;
    this.feedCache = feedCache;
    this.feedProviderService = feedProviderService;
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
      // if system_id is well known, feed could exist according to the spec but
      // is not available (e.g. due to upstream issues), we respond with 502 and not 4xx.
      // In case the feed is initialised (gbfs feed is available),
      // we throw 502 if the feed should exist according to the gbfs, else 404.
      if (feedProviderService.getFeedProviderBySystemId(systemId) != null) {
        if (mightOrShouldFeedExist(systemId, feed)) {
          throw new ResponseStatusException(HttpStatus.BAD_GATEWAY);
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
      }
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
  }

  /*
    Returns true, if either the discoveryFile (gbf file) is not yet cached,
    or the requested feed is published in the discovery file.
   */
  protected boolean mightOrShouldFeedExist(String systemId, String feed) {
    try {
      GBFS discoveryFile = (GBFS) getFeed(systemId, "gbfs");
      var feedName = GBFSFeedName.fromValue(feed);
      List<GBFSFeedName> feedNames =
        ((GBFS) discoveryFile).getFeedsData()
          .values()
          .stream()
          .map(GBFSFeeds::getFeeds)
          .flatMap(list -> list.stream())
          .map(GBFSFeed::getName)
          .toList();

      return feedNames.contains(feedName);
    } catch (NoSuchElementException e) {
      return true;
    } catch (NullPointerException e) {
      // in case the gbfs is malformed, e.g. no languages are defined, or no feeds,
      // this is an upstream error and the requested feed might exist
      return true;
    }
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
      throw new NoSuchElementException();
    }
    return data;
  }
}
