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
import java.util.Objects;
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

    // Set to false when a delta could not be applied, leaving a station missing
    // from the entity cache. Signals that delta continuity must be broken so the
    // next update performs a full rebuild instead of permanently losing the station.
    boolean fullyApplied = true;

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
  private final MetricsService metricsService;
  private final SpatialIndexIdGeneratorService spatialIndexService;

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

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

  /**
   * Applies a station status delta to the entity caches.
   *
   * @return true if the delta was fully applied, false if one or more stations
   * could not be added to the entity cache (e.g. due to missing station
   * information), in which case delta continuity should be broken to force a
   * full rebuild on the next update
   */
  public boolean update(
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

    return context.fullyApplied;
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

  public void clearExistingEntities(FeedProvider feedProvider) {
    var systemId = feedProvider.getSystemId();
    var existingStations = stationCache.getAll();
    var stationsToRemove = existingStations
      .stream()
      .filter(s -> systemId.equals(s.getSystemId()))
      .toList();

    if (!stationsToRemove.isEmpty()) {
      logger.debug(
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
        .map(s -> createStationIndexIdOrNull(s, feedProvider))
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());

      stationCache.removeAll(idsToRemove);
      spatialIndex.removeAll(spatialIdsToRemove);
    }
  }

  /**
   * Generates a station's spatial index id, returning null instead of throwing when the id
   * cannot be generated. Used where one unusable station must not abort the whole batch.
   */
  private StationSpatialIndexId createStationIndexIdOrNull(
    Station station,
    FeedProvider feedProvider
  ) {
    try {
      return spatialIndexService.createStationIndexId(station, feedProvider);
    } catch (IllegalStateException e) {
      logger.warn(
        "Could not generate spatial index id for station being cleared, leaving an orphan in the index for provider={} stationId={}",
        feedProvider,
        station.getId(),
        e
      );
      return null;
    }
  }

  private void processDeltaDelete(
    UpdateContext context,
    GBFSEntityDelta<GBFSStation> entityDelta,
    Station currentStation
  ) {
    context.stationIdsToRemove.add(entityDelta.entityId());
    if (currentStation != null) {
      try {
        context.spatialIndexIdsToRemove.add(
          spatialIndexService.createStationIndexId(currentStation, context.feedProvider)
        );
      } catch (IllegalStateException e) {
        // Still remove the station from the entity cache. Leaving an orphan behind in the
        // index is preferable to removing a key generated from degraded data, which would
        // delete the wrong entry and leave the real one in place.
        logger.warn(
          "Could not generate spatial index id for station marked for deletion, leaving an orphan in the index for provider={} stationId={}",
          context.feedProvider,
          entityDelta.entityId(),
          e
        );
      }
    } else {
      logger.debug(
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
        "Skipping station create due to missing station information feed for provider={} stationId={}",
        context.feedProvider,
        stationId
      );
      context.fullyApplied = false;
      return;
    }

    Station mappedStation = stationMapper.mapStation(
      stationInformation,
      entityDelta.entity(),
      context.feedProvider.getSystemId(),
      context.feedProvider.getLanguage()
    );

    StationSpatialIndexId spatialIndexId;
    try {
      spatialIndexId =
        spatialIndexService.createStationIndexId(mappedStation, context.feedProvider);
    } catch (IllegalStateException e) {
      logger.warn(
        "Skipping station create because its spatial index id could not be generated for provider={} stationId={}",
        context.feedProvider,
        stationId,
        e
      );
      return;
    }

    context.spatialIndexUpdateMap.put(spatialIndexId, mappedStation);

    context.addedAndUpdatedStations.put(mappedStation.getId(), mappedStation);
  }

  private void processDeltaUpdate(
    UpdateContext context,
    GBFSEntityDelta<GBFSStation> entityDelta,
    Station currentStation
  ) {
    var stationId = entityDelta.entityId();

    if (currentStation == null) {
      logger.warn(
        "Station marked for update but not found in cache for provider={} stationId={}",
        context.feedProvider,
        stationId
      );
      context.fullyApplied = false;
      return;
    }

    var stationInformation = context.stationInfo.get(stationId);
    if (stationInformation == null) {
      logger.warn(
        "Skipping station update due to missing station information feed for provider={} stationId={}",
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

    // Generate both keys before committing anything to the context, so that a failure
    // cannot leave the remove key applied without its matching add key.
    StationSpatialIndexId currentSpatialIndexId;
    StationSpatialIndexId updatedSpatialIndexId;
    try {
      currentSpatialIndexId =
        spatialIndexService.createStationIndexId(currentStation, context.feedProvider);
      updatedSpatialIndexId =
        spatialIndexService.createStationIndexId(mappedStation, context.feedProvider);
    } catch (IllegalStateException e) {
      logger.warn(
        "Skipping station update because its spatial index id could not be generated for provider={} stationId={}",
        context.feedProvider,
        stationId,
        e
      );
      return;
    }

    context.spatialIndexIdsToRemove.add(currentSpatialIndexId);

    context.addedAndUpdatedStations.put(mappedStation.getId(), mappedStation);

    context.spatialIndexUpdateMap.put(updatedSpatialIndexId, mappedStation);
  }

  private void updateCaches(UpdateContext context) {
    // Only delete index entries that are not about to be rewritten. For an ordinary status
    // change the removed and added keys are equal, since the key holds the available form
    // factors and propulsion types but not the vehicle counts, so the delete is a wasted
    // command. What is left is the genuine key change, which replaceAll makes atomic.
    var staleSpatialIndexIds = new HashSet<>(context.spatialIndexIdsToRemove);
    staleSpatialIndexIds.removeAll(context.spatialIndexUpdateMap.keySet());

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

    // Applied after the entity cache writes, so that an index entry is never visible before
    // the entity it points at. Deleting the stale key as part of the same operation means a
    // station changing its index key is never momentarily absent from the index.
    if (!staleSpatialIndexIds.isEmpty() || !context.spatialIndexUpdateMap.isEmpty()) {
      logger.debug(
        "Replacing {} stale entries with {} entries in spatial index",
        staleSpatialIndexIds.size(),
        context.spatialIndexUpdateMap.size()
      );
      spatialIndex.replaceAll(staleSpatialIndexIds, context.spatialIndexUpdateMap);
    }

    metricsService.registerEntityCount(
      MetricsService.ENTITY_STATION,
      stationCache.count()
    );
  }
}
