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
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.entur.lamassu.cache.GBFSV3FeedCache;
import org.entur.lamassu.model.provider.FeedProvider;
import org.entur.lamassu.service.FeedProviderService;
import org.entur.lamassu.service.SystemDiscoveryService;
import org.entur.lamassu.util.CacheUtil;
import org.jetbrains.annotations.NotNull;
import org.mobilitydata.gbfs.v3_0.gbfs.GBFSFeed;
import org.mobilitydata.gbfs.v3_0.gbfs.GBFSGbfs;
import org.mobilitydata.gbfs.v3_0.manifest.GBFSData;
import org.mobilitydata.gbfs.v3_0.manifest.GBFSDataset;
import org.mobilitydata.gbfs.v3_0.manifest.GBFSManifest;
import org.mobilitydata.gbfs.v3_0.manifest.GBFSVersion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
public class GBFSInternalV3FeedController {

  @Value("${org.entur.lamassu.gbfs.cache-control.minimum-ttl:0}")
  private int cacheControlMinimumTtl;

  private final SystemDiscoveryService systemDiscoveryService;

  private final FeedProviderService feedProviderService;

  private final GBFSV3FeedCache feedCache;

  @Value("${org.entur.lamassu.baseUrl}")
  private String baseUrl;

  @Value("${org.entur.lamassu.internalLoadBalancer}")
  private String internalLoadBalancer;

  @Autowired
  public GBFSInternalV3FeedController(
    SystemDiscoveryService systemDiscoveryService,
    GBFSV3FeedCache feedCache,
    FeedProviderService feedProviderService
  ) {
    this.systemDiscoveryService = systemDiscoveryService;
    this.feedCache = feedCache;
    this.feedProviderService = feedProviderService;
  }

  @GetMapping("/gbfs-internal/v3/manifest.json")
  public ResponseEntity<GBFSManifest> getInternalManifest() {
    var data = systemDiscoveryService.getGBFSManifest();
    return ResponseEntity
      .ok()
      .cacheControl(CacheControl.maxAge(3600, TimeUnit.MINUTES).cachePublic())
      .body(modifyManifestUrls(data));
  }

  @GetMapping(
    value = {
      "/gbfs-internal/v3/{systemId}/{feed}", "/gbfs-internal/v3/{systemId}/{feed}.json",
    }
  )
  public Object getInternalGbfsFeedForProvider(
    @PathVariable String systemId,
    @PathVariable String feed
  ) {
    try {
      var feedName = GBFSFeed.Name.fromValue(feed);
      Object data = getFeed(systemId, feed);
      if (feedName.equals(GBFSFeed.Name.GBFS)) {
        data = modifyDiscoveryUrls((GBFSGbfs) data);
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
        .body(data);
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    } catch (NoSuchElementException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
  }

  private GBFSManifest modifyManifestUrls(GBFSManifest data) {
    var manifest = new GBFSManifest();
    manifest.setLastUpdated(data.getLastUpdated());
    manifest.setTtl(data.getTtl());

    manifest.setData(
      new GBFSData()
        .withDatasets(
          data
            .getData()
            .getDatasets()
            .stream()
            .map(dataset -> {
              var modifiedDataset = new GBFSDataset();
              modifiedDataset.setSystemId(dataset.getSystemId());
              modifiedDataset.setVersions(
                dataset
                  .getVersions()
                  .stream()
                  .map(version -> {
                    var modifiedVersion = new GBFSVersion();
                    modifiedVersion.setVersion(version.getVersion());
                    modifiedVersion.setUrl(
                      URI
                        .create(
                          version
                            .getUrl()
                            .replace(
                              baseUrl + "/gbfs/v3",
                              internalLoadBalancer + "/gbfs-internal/v3"
                            )
                        )
                        .toString()
                    );
                    return modifiedVersion;
                  })
                  .collect(Collectors.toList())
              );
              return modifiedDataset;
            })
            .toList()
        )
    );
    return manifest;
  }

  private GBFSGbfs modifyDiscoveryUrls(GBFSGbfs data) {
    var gbfs = new GBFSGbfs();
    gbfs.setLastUpdated(data.getLastUpdated());
    gbfs.setTtl(data.getTtl());
    gbfs.setVersion(data.getVersion());

    var gbfsData = new org.mobilitydata.gbfs.v3_0.gbfs.GBFSData();
    gbfsData.setFeeds(
      data
        .getData()
        .getFeeds()
        .stream()
        .map(f -> {
          var gbfsFeed = new GBFSFeed();
          gbfsFeed.setName(f.getName());
          gbfsFeed.setUrl(
            URI
              .create(
                f
                  .getUrl()
                  .replace(
                    baseUrl + "/gbfs/v3",
                    internalLoadBalancer + "/gbfs-internal/v3"
                  )
              )
              .toString()
          );
          return gbfsFeed;
        })
        .collect(Collectors.toList())
    );
    gbfs.setData(gbfsData);
    return gbfs;
  }

  @NotNull
  protected Object getFeed(String systemId, String feed) {
    var feedName = GBFSFeed.Name.fromValue(feed);
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
      Throws an UpstreamFeedNotYetAvailableException, if either the discoveryFile (gbf file) is not yet cached,
      the requested feed is published in the discovery file, or the discovery file is malformed.
     */
  protected void throwsIfFeedCouldOrShouldExist(
    GBFSFeed.Name feedName,
    FeedProvider feedProvider
  ) {
    try {
      GBFSGbfs discoveryFile = feedCache.find(GBFSFeed.Name.GBFS, feedProvider);
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
