package org.entur.lamassu.controller;

import java.net.URI;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.entur.gbfs.mapper.GBFSMapper;
import org.entur.gbfs.v2_3.free_bike_status.GBFSFreeBikeStatus;
import org.entur.gbfs.v2_3.gbfs.GBFS;
import org.entur.gbfs.v2_3.gbfs.GBFSFeed;
import org.entur.gbfs.v2_3.gbfs.GBFSFeedName;
import org.entur.gbfs.v2_3.gbfs.GBFSFeeds;
import org.entur.gbfs.v2_3.geofencing_zones.GBFSGeofencingZones;
import org.entur.gbfs.v2_3.station_information.GBFSStationInformation;
import org.entur.gbfs.v2_3.station_status.GBFSStationStatus;
import org.entur.gbfs.v2_3.system_alerts.GBFSSystemAlerts;
import org.entur.gbfs.v2_3.system_information.GBFSSystemInformation;
import org.entur.gbfs.v2_3.system_pricing_plans.GBFSSystemPricingPlans;
import org.entur.gbfs.v2_3.system_regions.GBFSSystemRegions;
import org.entur.gbfs.v2_3.vehicle_types.GBFSVehicleTypes;
import org.entur.gbfs.v3_0_RC.manifest.GBFSData;
import org.entur.gbfs.v3_0_RC.manifest.GBFSDataset;
import org.entur.gbfs.v3_0_RC.manifest.GBFSManifest;
import org.entur.gbfs.v3_0_RC.manifest.GBFSVersion;
import org.entur.lamassu.cache.GBFSFeedCache;
import org.entur.lamassu.model.discovery.System;
import org.entur.lamassu.model.discovery.SystemDiscovery;
import org.entur.lamassu.model.provider.FeedProvider;
import org.entur.lamassu.service.FeedProviderService;
import org.entur.lamassu.service.SystemDiscoveryService;
import org.entur.lamassu.util.CacheUtil;
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
  public GBFSFeedController(
    SystemDiscoveryService systemDiscoveryService,
    GBFSFeedCache feedCache,
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

  @GetMapping("/gbfs-internal")
  public ResponseEntity<SystemDiscovery> getInternalFeedProviderDiscovery() {
    var data = systemDiscoveryService.getSystemDiscovery();
    return ResponseEntity
      .ok()
      .cacheControl(CacheControl.maxAge(60, TimeUnit.MINUTES).cachePublic())
      .body(modifySystemDiscoveryUrls(data));
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
            s.getUrl().replace(baseUrl + "/gbfs", internalLoadBalancer + "/gbfs-internal")
          );
          return system;
        })
        .collect(Collectors.toList())
    );
    return systemDiscovery;
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
              CacheUtil.getMaxAge(feedName.implementingClass(), data),
              TimeUnit.SECONDS
            )
            .cachePublic()
        )
        .body(data);
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    } catch (NoSuchElementException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
  }

  @GetMapping(
    value = {
      "/gbfs-internal/{systemId}/{feed}", "/gbfs-internal/{systemId}/{feed}.json",
    }
  )
  public Object getInternalGbfsFeedForProvider(
    @PathVariable String systemId,
    @PathVariable String feed
  ) {
    try {
      var feedName = GBFSFeedName.fromValue(feed);
      var feedProvider = feedProviderService.getFeedProviderBySystemId(systemId);
      Object data = getFeed(systemId, feed);
      if (feedName.equals(GBFSFeedName.GBFS)) {
        data = modifyDiscoveryUrls(feedProvider, (GBFS) data);
      }

      return ResponseEntity
        .ok()
        .cacheControl(
          CacheControl
            .maxAge(
              CacheUtil.getMaxAge(feedName.implementingClass(), data),
              TimeUnit.SECONDS
            )
            .cachePublic()
        )
        .body(data);
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    } catch (NoSuchElementException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
  }

  @GetMapping("/gbfs/v3beta/manifest.json")
  public ResponseEntity<GBFSManifest> getV3Manifest() {
    var data = systemDiscoveryService.getSystemDiscovery();

    var manifest = new GBFSManifest()
      .withVersion(GBFSManifest.Version._3_0_RC)
      .withLastUpdated((int) java.lang.System.currentTimeMillis() / 1000)
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
                        .withVersion(GBFSVersion.Version._3_0_RC)
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

      var feedName = org.entur.gbfs.v3_0_RC.gbfs.GBFSFeed.Name.fromValue(feed);

      return ResponseEntity
        .ok()
        .cacheControl(
          CacheControl
            .maxAge(
              CacheUtil.getMaxAge(
                org.entur.gbfs.v3_0_RC.gbfs.GBFSFeedName.implementingClass(feedName),
                mapped
              ),
              TimeUnit.SECONDS
            )
            .cachePublic()
        )
        .body(mapped);
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    } catch (NoSuchElementException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
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
                      .replace(baseUrl + "/gbfs", internalLoadBalancer + "/gbfs-internal")
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
}
