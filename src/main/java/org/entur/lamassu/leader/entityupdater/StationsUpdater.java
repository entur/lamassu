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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.entur.lamassu.cache.EntityCache;
import org.entur.lamassu.cache.StationSpatialIndex;
import org.entur.lamassu.cache.StationSpatialIndexId;
import org.entur.lamassu.delta.DeltaType;
import org.entur.lamassu.delta.GBFSEntityDelta;
import org.entur.lamassu.delta.GBFSFileDelta;
import org.entur.lamassu.delta.GBFSStationStatusDeltaCalculator;
import org.entur.lamassu.mapper.entitymapper.StationMapper;
import org.entur.lamassu.mapper.entitymapper.StationMergeMapper;
import org.entur.lamassu.metrics.MetricsService;
import org.entur.lamassu.model.entities.Station;
import org.entur.lamassu.model.provider.FeedProvider;
import org.entur.lamassu.service.SpatialIndexIdGeneratorService;
import org.mobilitydata.gbfs.v3_0.station_information.GBFSData;
import org.mobilitydata.gbfs.v3_0.station_information.GBFSStationInformation;
import org.mobilitydata.gbfs.v3_0.station_status.GBFSStation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class StationsUpdater {

  private static final class UpdateContext {

    final FeedProvider feedProvider;
    final int ttl;
    final Map<String, org.mobilitydata.gbfs.v3_0.station_information.GBFSStation> stationInfo;

    final Set<String> stationIdsToRemove = new HashSet<>();
    final Map<String, Station> addedAndUpdatedStations = new HashMap<>();
    final Set<StationSpatialIndexId> spatialIndexIdsToRemove = new HashSet<>();
    final Map<StationSpatialIndexId, Station> spatialIndexUpdateMap = new HashMap<>();

    public UpdateContext(
      FeedProvider feedProvider,
      int ttl,
      Map<String, org.mobilitydata.gbfs.v3_0.station_information.GBFSStation> stationInfo
    ) {
      this.feedProvider = feedProvider;
      this.ttl = ttl;
      this.stationInfo = stationInfo;
    }
  }

  private final EntityCache<Station> stationCache;
  private final StationSpatialIndex spatialIndex;
  private final StationMapper stationMapper;
  private final StationMergeMapper stationMergeMapper;
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
    StationMergeMapper stationMergeMapper,
    MetricsService metricsService,
    SpatialIndexIdGeneratorService spatialIndexService
  ) {
    this.stationCache = stationCache;
    this.spatialIndex = spatialIndex;
    this.stationMapper = stationMapper;
    this.stationMergeMapper = stationMergeMapper;
    this.metricsService = metricsService;
    this.spatialIndexService = spatialIndexService;
  }

  public void addOrUpdateStations(
    FeedProvider feedProvider,
    GBFSFileDelta<GBFSStation> delta,
    GBFSStationInformation stationInformationFeed
  ) {
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

    UpdateContext context = new UpdateContext(
      feedProvider,
      delta.ttl().intValue(),
      stationInfo
    );

    for (GBFSEntityDelta<GBFSStation> entityDelta : delta.entityDelta()) {
      Station currentStation = stationCache.get(entityDelta.entityId());

      if (entityDelta.type() == DeltaType.DELETE) {
        processDeltaDelete(context, entityDelta, currentStation);
      } else if (entityDelta.type() == DeltaType.CREATE) {
        processDeltaCreate(context, entityDelta);
      } else if (entityDelta.type() == DeltaType.UPDATE) {
        processDeltaUpdate(context, entityDelta, currentStation);
      }
    }

    updateCaches(context);
  }

  private void processDeltaDelete(
    UpdateContext context,
    GBFSEntityDelta<GBFSStation> entityDelta,
    Station currentStation
  ) {
    context.stationIdsToRemove.add(entityDelta.entityId());
    if (currentStation != null) {
      var spatialIndexId = spatialIndexService.createStationIndexId(
        currentStation,
        context.feedProvider
      );
      context.spatialIndexIdsToRemove.add(spatialIndexId);
    } else {
      logger.warn(
        "Station {} marked for deletion but not found in cache",
        entityDelta.entityId()
      );
    }
  }

  private void processDeltaCreate(
    UpdateContext context,
    GBFSEntityDelta<GBFSStation> entityDelta
  ) {
    var stationId = entityDelta.entityId();
    var stationInformation = context.stationInfo.get(stationId);
    if (stationInformation == null) {
      logger.warn(
        "Skipping station due to missing station information feed for provider={} stationId={}",
        context.feedProvider,
        stationId
      );
      return;
    }

    Station mappedStation = stationMapper.mapStation(
      stationInformation,
      entityDelta.entity(),
      context.feedProvider.getSystemId(),
      context.feedProvider.getLanguage()
    );
    context.addedAndUpdatedStations.put(mappedStation.getId(), mappedStation);
    updateSpatialIndex(context, mappedStation);
  }

  private void processDeltaUpdate(
    UpdateContext context,
    GBFSEntityDelta<GBFSStation> entityDelta,
    Station currentStation
  ) {
    var stationId = entityDelta.entityId();
    var stationInformation = context.stationInfo.get(stationId);
    if (stationInformation == null) {
      logger.warn(
        "Skipping station due to missing station information feed for provider={} stationId={}",
        context.feedProvider,
        stationId
      );
      return;
    }

    if (currentStation != null) {
      Station mappedStation = stationMapper.mapStation(
        stationInformation,
        entityDelta.entity(),
        context.feedProvider.getSystemId(),
        context.feedProvider.getLanguage()
      );

      // For updates, we need to check if the spatial index needs updating
      var oldSpatialIndexId = spatialIndexService.createStationIndexId(
        currentStation,
        context.feedProvider
      );
      var newSpatialIndexId = spatialIndexService.createStationIndexId(
        mappedStation,
        context.feedProvider
      );
      if (!oldSpatialIndexId.equals(newSpatialIndexId)) {
        context.spatialIndexIdsToRemove.add(oldSpatialIndexId);
      }

      // Merge the mapped station into the current station
      stationMergeMapper.updateStation(currentStation, mappedStation);
      context.addedAndUpdatedStations.put(currentStation.getId(), currentStation);
      updateSpatialIndex(context, currentStation);
    } else {
      logger.warn(
        "Station {} marked for update but not found in cache",
        entityDelta.entityId()
      );
    }
  }

  private void updateSpatialIndex(UpdateContext context, Station station) {
    var spatialIndexId = spatialIndexService.createStationIndexId(
      station,
      context.feedProvider
    );
    context.spatialIndexUpdateMap.put(spatialIndexId, station);
  }

  private void updateCaches(UpdateContext context) {
    if (!context.spatialIndexIdsToRemove.isEmpty()) {
      logger.debug(
        "Removing {} stale entries in spatial index",
        context.spatialIndexIdsToRemove.size()
      );
      spatialIndex.removeAll(context.spatialIndexIdsToRemove);
    }

    if (!context.stationIdsToRemove.isEmpty()) {
      logger.debug(
        "Removing {} stations from station cache",
        context.stationIdsToRemove.size()
      );
      stationCache.removeAll(context.stationIdsToRemove);
    }

    if (!context.addedAndUpdatedStations.isEmpty()) {
      logger.debug(
        "Adding/updating {} stations in station cache",
        context.addedAndUpdatedStations.size()
      );
      stationCache.updateAll(
        context.addedAndUpdatedStations,
        context.ttl,
        TimeUnit.SECONDS
      );
    }

    if (!context.spatialIndexUpdateMap.isEmpty()) {
      logger.debug(
        "Updating {} entries in spatial index",
        context.spatialIndexUpdateMap.size()
      );
      spatialIndex.addAll(context.spatialIndexUpdateMap);
    }

    metricsService.registerEntityCount(
      MetricsService.ENTITY_STATION,
      stationCache.count()
    );
  }
}
