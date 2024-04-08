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
import org.entur.gbfs.v3_0_RC2.gbfs.GBFSFeed;
import org.entur.gbfs.v3_0_RC2.manifest.GBFSManifest;
import org.entur.lamassu.cache.GBFSV3FeedCache;
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
public class GBFSV3FeedController {

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

  @GetMapping("/gbfs/v3beta/manifest.json")
  public ResponseEntity<GBFSManifest> getV3Manifest() {
    var manifest = systemDiscoveryService.getGBFSManifest();

    return ResponseEntity
      .ok()
      .cacheControl(CacheControl.maxAge(3600, TimeUnit.MINUTES).cachePublic())
      .body(manifest);
  }

  @GetMapping(
    value = { "/gbfs/v3beta/{systemId}/{feed}", "/gbfs/v3beta/{systemId}/{feed}.json" }
  )
  public ResponseEntity<Object> getV3Feed(
    @PathVariable String systemId,
    @PathVariable String feed
  ) {
    try {
      var feedName = GBFSFeed.Name.fromValue(feed);

      var data = getFeed(systemId, feed);

      return ResponseEntity
        .ok()
        .cacheControl(
          CacheControl
            .maxAge(
              CacheUtil.getMaxAge(
                org.entur.gbfs.v3_0_RC2.gbfs.GBFSFeedName.implementingClass(feedName),
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
          CacheUtil.getLastModified(
            org.entur.gbfs.v3_0_RC2.gbfs.GBFSFeedName.implementingClass(feedName),
            data,
            systemId,
            feed
          )
        )
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
      throw new NoSuchElementException();
    }
    return data;
  }
}
