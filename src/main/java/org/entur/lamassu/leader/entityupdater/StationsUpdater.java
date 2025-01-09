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

package org.entur.lamassu.leader.entityupdater;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.entur.gbfs.loader.v3.GbfsV3Delivery;
import org.entur.lamassu.cache.EntityCache;
import org.entur.lamassu.cache.StationSpatialIndex;
import org.entur.lamassu.cache.StationSpatialIndexId;
import org.entur.lamassu.mapper.entitymapper.StationMapper;
import org.entur.lamassu.metrics.MetricsService;
import org.entur.lamassu.model.entities.Station;
import org.entur.lamassu.model.provider.FeedProvider;
import org.entur.lamassu.service.SpatialIndexIdGeneratorService;
import org.entur.lamassu.util.CacheUtil;
import org.mobilitydata.gbfs.v3_0.station_information.GBFSData;
import org.mobilitydata.gbfs.v3_0.station_information.GBFSStationInformation;
import org.mobilitydata.gbfs.v3_0.station_status.GBFSStation;
import org.mobilitydata.gbfs.v3_0.station_status.GBFSStationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class StationsUpdater {

  private final EntityCache<Station> stationCache;
  private final StationSpatialIndex spatialIndex;
  private final StationMapper stationMapper;
  private final MetricsService metricsService;
  private final SpatialIndexIdGeneratorService spatialIndexService;
  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  @Value("${org.entur.lamassu.stationEntityCacheMinimumTtl:30}")
  private Integer stationEntityCacheMinimumTtl;

  @Value("${org.entur.lamassu.stationEntityCacheMaximumTtl:300}")
  private Integer stationEntityCacheMaximumTtl;

  @Autowired
  public StationsUpdater(
    EntityCache<Station> stationCache,
    StationSpatialIndex spatialIndex,
    StationMapper stationMapper,
    MetricsService metricsService,
    SpatialIndexIdGeneratorService spatialIndexService
  ) {
    this.stationCache = stationCache;
    this.spatialIndex = spatialIndex;
    this.stationMapper = stationMapper;
    this.metricsService = metricsService;
    this.spatialIndexService = spatialIndexService;
  }

  public void addOrUpdateStations(
    FeedProvider feedProvider,
    GbfsV3Delivery delivery,
    GbfsV3Delivery oldDelivery
  ) {
    GBFSStationStatus stationStatusFeed = delivery.stationStatus();
    GBFSStationStatus oldStationStatusFeed = oldDelivery.stationStatus();
    GBFSStationInformation stationInformationFeed = delivery.stationInformation();

    var stationIds = stationStatusFeed
      .getData()
      .getStations()
      .stream()
      .map(GBFSStation::getStationId)
      .collect(Collectors.toSet());

    Set<String> stationIdsToRemove = null;

    if (oldStationStatusFeed != null && oldStationStatusFeed.getData() != null) {
      stationIdsToRemove =
        oldStationStatusFeed
          .getData()
          .getStations()
          .stream()
          .map(GBFSStation::getStationId)
          .collect(Collectors.toSet());
      stationIdsToRemove.removeAll(stationIds);
      logger.debug(
        "Found {} stationIds to remove from old station_status feed",
        stationIdsToRemove.size()
      );

      // Add station ids that are staged for removal to the set of stations ids that will be used to
      // fetch current stations from cache
      stationIds.addAll(stationIdsToRemove);
    }

    if (stationIdsToRemove == null) {
      stationIdsToRemove = new HashSet<>(stationIds);
      logger.debug(
        "Old station_status feed was not available or had no data. As a workaround, removing all stations for provider {}",
        feedProvider.getSystemId()
      );
    }

    var originalStations = stationCache.getAllAsMap(stationIds);

    var stationInfo = Optional
      .ofNullable(stationInformationFeed)
      .map(GBFSStationInformation::getData)
      .map(GBFSData::getStations)
      .orElse(List.of())
      .stream()
      .collect(
        Collectors.toMap(
          org.mobilitydata.gbfs.v3_0.station_information.GBFSStation::getStationId,
          s -> s
        )
      );

    var stations = stationStatusFeed
      .getData()
      .getStations()
      .stream()
      .filter(s -> {
        if (stationInfo.get(s.getStationId()) == null) {
          logger.warn(
            "Skipping station due to missing station information feed for provider={} stationId={}",
            feedProvider,
            s.getStationId()
          );
          return false;
        }
        return true;
      })
      .map(station ->
        stationMapper.mapStation(
          stationInfo.get(station.getStationId()),
          station,
          feedProvider.getSystemId(),
          feedProvider.getLanguage()
        )
      )
      .collect(Collectors.toMap(Station::getId, s -> s));

    Set<StationSpatialIndexId> spatialIndicesToRemove = new java.util.HashSet<>(Set.of());
    Map<StationSpatialIndexId, Station> spatialIndexUpdateMap = new java.util.HashMap<>(
      Map.of()
    );

    stations.forEach((key, station) -> {
      var spatialIndexId = spatialIndexService.createStationIndexId(
        station,
        feedProvider
      );
      var previousStation = originalStations.get(key);

      if (previousStation != null) {
        var oldSpatialIndexId = spatialIndexService.createStationIndexId(
          previousStation,
          feedProvider
        );
        if (!oldSpatialIndexId.equals(spatialIndexId)) {
          spatialIndicesToRemove.add(oldSpatialIndexId);
        }
      }
      spatialIndexUpdateMap.put(spatialIndexId, station);
    });

    if (!spatialIndicesToRemove.isEmpty()) {
      logger.debug(
        "Removing {} stale entries in spatial index",
        spatialIndicesToRemove.size()
      );
      spatialIndex.removeAll(spatialIndicesToRemove);
    }

    if (!stationIdsToRemove.isEmpty()) {
      logger.debug("Removing {} stations from station cache", stationIdsToRemove.size());
      stationCache.removeAll(stationIdsToRemove);
    }

    if (!stations.isEmpty()) {
      logger.debug("Adding/updating {} stations in station cache", stations.size());
      var lastUpdated = stationStatusFeed.getLastUpdated();
      var ttl = stationStatusFeed.getTtl();
      stationCache.updateAll(
        stations,
        CacheUtil.getTtl(
          (int) Instant.now().getEpochSecond(),
          (int) lastUpdated.getTime() / 1000,
          ttl,
          stationEntityCacheMinimumTtl,
          stationEntityCacheMaximumTtl
        ),
        TimeUnit.SECONDS
      );
    }

    if (!spatialIndexUpdateMap.isEmpty()) {
      logger.debug("Updating {} entries in spatial index", spatialIndexUpdateMap.size());
      spatialIndex.addAll(spatialIndexUpdateMap);
    }

    metricsService.registerEntityCount(
      MetricsService.ENTITY_STATION,
      stationCache.count()
    );
  }
}
