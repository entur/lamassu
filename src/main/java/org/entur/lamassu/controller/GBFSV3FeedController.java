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
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.entur.gbfs.mapper.GBFSMapper;
import org.entur.gbfs.v2_3.free_bike_status.GBFSFreeBikeStatus;
import org.entur.gbfs.v2_3.gbfs.GBFS;
import org.entur.gbfs.v2_3.geofencing_zones.GBFSGeofencingZones;
import org.entur.gbfs.v2_3.station_information.GBFSStationInformation;
import org.entur.gbfs.v2_3.station_status.GBFSStationStatus;
import org.entur.gbfs.v2_3.system_alerts.GBFSSystemAlerts;
import org.entur.gbfs.v2_3.system_information.GBFSSystemInformation;
import org.entur.gbfs.v2_3.system_pricing_plans.GBFSSystemPricingPlans;
import org.entur.gbfs.v2_3.system_regions.GBFSSystemRegions;
import org.entur.gbfs.v2_3.vehicle_types.GBFSVehicleTypes;
import org.entur.gbfs.v3_0_RC2.manifest.GBFSData;
import org.entur.gbfs.v3_0_RC2.manifest.GBFSDataset;
import org.entur.gbfs.v3_0_RC2.manifest.GBFSManifest;
import org.entur.gbfs.v3_0_RC2.manifest.GBFSVersion;
import org.entur.lamassu.cache.GBFSFeedCache;
import org.entur.lamassu.service.FeedProviderService;
import org.entur.lamassu.service.SystemDiscoveryService;
import org.entur.lamassu.util.CacheUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class GBFSV3FeedController extends BaseGBFSFeedController {

  private final SystemDiscoveryService systemDiscoveryService;
  private final FeedProviderService feedProviderService;

  @Value("${org.entur.lamassu.baseUrl}")
  private String baseUrl;

  @Autowired
  public GBFSV3FeedController(
    SystemDiscoveryService systemDiscoveryService,
    GBFSFeedCache feedCache,
    FeedProviderService feedProviderService
  ) {
    super(feedCache, feedProviderService);
    this.systemDiscoveryService = systemDiscoveryService;
    this.feedProviderService = feedProviderService;
  }

  @GetMapping("/gbfs/v3beta/manifest.json")
  public ResponseEntity<GBFSManifest> getV3Manifest() {
    var data = systemDiscoveryService.getSystemDiscovery();

    var manifest = new GBFSManifest()
      .withVersion(GBFSManifest.Version._3_0_RC_2)
      .withLastUpdated(new Date())
      .withTtl(3600)
      .withData(
        new GBFSData()
          .withDatasets(
            data
              .getSystems()
              .stream()
              .map(system ->
                new GBFSDataset()
                  .withSystemId(system.getId())
                  .withVersions(
                    List.of(
                      new GBFSVersion()
                        .withVersion(GBFSVersion.Version._2_3)
                        .withUrl(system.getUrl()),
                      new GBFSVersion()
                        .withVersion(GBFSVersion.Version._3_0_RC_2)
                        .withUrl(baseUrl + "/gbfs/v3beta/" + system.getId() + "/gbfs")
                    )
                  )
              )
              .collect(Collectors.toList())
          )
      );

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
      Object mapped = null;

      var feedProvider = feedProviderService.getFeedProviderBySystemId(systemId);
      var data = getFeed(
        systemId,
        feed.equals("vehicle_status") ? "free_bike_status" : feed
      );

      if (data instanceof GBFS) {
        var tmp = GBFSMapper.INSTANCE.map((GBFS) data, feedProvider.getLanguage());
        tmp
          .getData()
          .getFeeds()
          .stream()
          .forEach(localFeed -> {
            localFeed.setUrl(
              baseUrl + "/gbfs/v3beta/" + systemId + "/" + localFeed.getName()
            );
          });
        mapped = tmp;
      } else if (data instanceof GBFSVehicleTypes) {
        mapped =
          GBFSMapper.INSTANCE.map((GBFSVehicleTypes) data, feedProvider.getLanguage());
      } else if (data instanceof GBFSSystemPricingPlans) {
        mapped =
          GBFSMapper.INSTANCE.map(
            (GBFSSystemPricingPlans) data,
            feedProvider.getLanguage()
          );
      } else if (data instanceof GBFSFreeBikeStatus) {
        mapped =
          GBFSMapper.INSTANCE.map((GBFSFreeBikeStatus) data, feedProvider.getLanguage());
      } else if (data instanceof GBFSStationInformation) {
        mapped =
          GBFSMapper.INSTANCE.map(
            (GBFSStationInformation) data,
            feedProvider.getLanguage()
          );
      } else if (data instanceof GBFSStationStatus) {
        mapped =
          GBFSMapper.INSTANCE.map((GBFSStationStatus) data, feedProvider.getLanguage());
      } else if (data instanceof GBFSSystemRegions) {
        mapped =
          GBFSMapper.INSTANCE.map((GBFSSystemRegions) data, feedProvider.getLanguage());
      } else if (data instanceof GBFSSystemAlerts) {
        mapped =
          GBFSMapper.INSTANCE.map((GBFSSystemAlerts) data, feedProvider.getLanguage());
      } else if (data instanceof GBFSGeofencingZones) {
        mapped =
          GBFSMapper.INSTANCE.map((GBFSGeofencingZones) data, feedProvider.getLanguage());
      } else if (data instanceof GBFSSystemInformation) {
        mapped =
          GBFSMapper.INSTANCE.map(
            (GBFSSystemInformation) data,
            feedProvider.getLanguage()
          );
      } else {
        throw new NoSuchElementException();
      }

      var feedName = org.entur.gbfs.v3_0_RC2.gbfs.GBFSFeed.Name.fromValue(feed);

      return ResponseEntity
        .ok()
        .cacheControl(
          CacheControl
            .maxAge(
              CacheUtil.getMaxAge(
                org.entur.gbfs.v3_0_RC2.gbfs.GBFSFeedName.implementingClass(feedName),
                mapped,
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
            mapped,
            systemId,
            feed
          )
        )
        .body(mapped);
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    } catch (NoSuchElementException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
  }
}
