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
import java.util.stream.Collectors;
import org.entur.lamassu.cache.EntityCache;
import org.entur.lamassu.cache.StationSpatialIndex;
import org.entur.lamassu.cache.StationSpatialIndexId;
import org.entur.lamassu.delta.DeltaType;
import org.entur.lamassu.delta.GBFSEntityDelta;
import org.entur.lamassu.delta.GBFSFileDelta;
import org.entur.lamassu.mapper.entitymapper.StationMapper;
import org.entur.lamassu.mapper.entitymapper.StationMergeMapper;
import org.entur.lamassu.metrics.MetricsService;
import org.entur.lamassu.model.entities.Station;
import org.entur.lamassu.model.provider.FeedProvider;
import org.entur.lamassu.service.SpatialIndexIdGeneratorService;
import org.jetbrains.annotations.NotNull;
import org.mobilitydata.gbfs.v3_0.station_information.GBFSData;
import org.mobilitydata.gbfs.v3_0.station_information.GBFSStationInformation;
import org.mobilitydata.gbfs.v3_0.station_status.GBFSStation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StationsUpdater {

  private static final class UpdateContext {

    final FeedProvider feedProvider;
    final Map<String, org.mobilitydata.gbfs.v3_0.station_information.GBFSStation> stationInfo;

    final Set<String> stationIdsToRemove = new HashSet<>();
    final Map<String, Station> addedAndUpdatedStations = new HashMap<>();
    final Set<StationSpatialIndexId> spatialIndexIdsToRemove = new HashSet<>();
    final Map<StationSpatialIndexId, Station> spatialIndexUpdateMap = new HashMap<>();

    public UpdateContext(
      FeedProvider feedProvider,
      Map<String, org.mobilitydata.gbfs.v3_0.station_information.GBFSStation> stationInfo
    ) {
      this.feedProvider = feedProvider;
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

  public void update(
    FeedProvider feedProvider,
    GBFSFileDelta<GBFSStation> delta,
    GBFSStationInformation stationInformationFeed
  ) {
    if (delta.base() == null) {
      clearExistingEntities(feedProvider);
    }

    var stationInfo = extractStationInfo(stationInformationFeed);
    UpdateContext context = new UpdateContext(feedProvider, stationInfo);

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

  private static @NotNull Map<String, org.mobilitydata.gbfs.v3_0.station_information.@NotNull GBFSStation> extractStationInfo(
    GBFSStationInformation stationInformationFeed
  ) {
    return Optional
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
  }

  private void clearExistingEntities(FeedProvider feedProvider) {
    var systemId = feedProvider.getSystemId();
    var existingStations = stationCache.getAll();
    var stationsToRemove = existingStations
      .stream()
      .filter(s -> systemId.equals(s.getSystemId()))
      .toList();

    if (!stationsToRemove.isEmpty()) {
      logger.info(
        "Removing {} existing stations for system {} due to null base",
        stationsToRemove.size(),
        systemId
      );

      var idsToRemove = stationsToRemove
        .stream()
        .map(Station::getId)
        .collect(Collectors.toSet());
      var spatialIdsToRemove = stationsToRemove
        .stream()
        .map(s -> spatialIndexService.createStationIndexId(s, feedProvider))
        .collect(Collectors.toSet());

      stationCache.removeAll(idsToRemove);
      spatialIndex.removeAll(spatialIdsToRemove);
    }
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

    var spatialIndexId = spatialIndexService.createStationIndexId(
      mappedStation,
      context.feedProvider
    );

    context.spatialIndexUpdateMap.put(spatialIndexId, mappedStation);

    context.addedAndUpdatedStations.put(mappedStation.getId(), mappedStation);
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

      context.spatialIndexIdsToRemove.add(
        spatialIndexService.createStationIndexId(currentStation, context.feedProvider)
      );

      // Merge the mapped station into the current station
      stationMergeMapper.updateStation(currentStation, mappedStation);
      context.addedAndUpdatedStations.put(currentStation.getId(), currentStation);

      context.spatialIndexUpdateMap.put(
        spatialIndexService.createStationIndexId(currentStation, context.feedProvider),
        currentStation
      );
    } else {
      logger.warn(
        "Station {} marked for update but not found in cache",
        entityDelta.entityId()
      );
    }
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
      stationCache.updateAll(context.addedAndUpdatedStations);
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
