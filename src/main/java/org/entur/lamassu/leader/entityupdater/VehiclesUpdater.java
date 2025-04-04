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
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.entur.lamassu.cache.EntityCache;
import org.entur.lamassu.cache.VehicleSpatialIndex;
import org.entur.lamassu.cache.VehicleSpatialIndexId;
import org.entur.lamassu.delta.DeltaType;
import org.entur.lamassu.delta.GBFSEntityDelta;
import org.entur.lamassu.delta.GBFSFileDelta;
import org.entur.lamassu.mapper.entitymapper.VehicleMapper;
import org.entur.lamassu.metrics.MetricsService;
import org.entur.lamassu.model.entities.Station;
import org.entur.lamassu.model.entities.Vehicle;
import org.entur.lamassu.model.provider.FeedProvider;
import org.entur.lamassu.service.SpatialIndexIdGeneratorService;
import org.mobilitydata.gbfs.v3_0.vehicle_status.GBFSVehicle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class VehiclesUpdater {

  private static final class UpdateContext {

    final FeedProvider feedProvider;

    final Set<String> vehicleIdsToRemove = new HashSet<>();
    final Map<String, Vehicle> addedAndUpdatedVehicles = new HashMap<>();
    final Set<VehicleSpatialIndexId> spatialIndexIdsToRemove = new HashSet<>();
    final Map<VehicleSpatialIndexId, Vehicle> spatialIndexUpdateMap = new HashMap<>();

    public UpdateContext(FeedProvider feedProvider) {
      this.feedProvider = feedProvider;
    }
  }

  private final EntityCache<Vehicle> vehicleCache;
  private final VehicleSpatialIndex spatialIndex;
  private final EntityCache<Station> stationCache;
  private final VehicleMapper vehicleMapper;
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private final MetricsService metricsService;
  private final SpatialIndexIdGeneratorService spatialIndexService;
  private final VehicleFilter vehicleFilter;

  @Autowired
  public VehiclesUpdater(
    EntityCache<Vehicle> vehicleCache,
    VehicleSpatialIndex spatialIndex,
    VehicleMapper vehicleMapper,
    MetricsService metricsService,
    SpatialIndexIdGeneratorService spatialIndexService,
    VehicleFilter vehicleFilter,
    EntityCache<Station> stationCache
  ) {
    this.vehicleCache = vehicleCache;
    this.spatialIndex = spatialIndex;
    this.vehicleMapper = vehicleMapper;
    this.metricsService = metricsService;
    this.spatialIndexService = spatialIndexService;
    this.vehicleFilter = vehicleFilter;
    this.stationCache = stationCache;
  }

  public void update(FeedProvider feedProvider, GBFSFileDelta<GBFSVehicle> delta) {
    if (delta.base() == null) {
      clearExistingEntities(feedProvider);
    }

    UpdateContext context = new UpdateContext(feedProvider);

    for (GBFSEntityDelta<GBFSVehicle> entityDelta : delta.entityDelta()) {
      if (entityDelta.type() == DeltaType.DELETE) {
        processDeltaDelete(context, entityDelta);
      } else if (entityDelta.type() == DeltaType.CREATE) {
        processDeltaCreate(context, entityDelta);
      } else if (entityDelta.type() == DeltaType.UPDATE) {
        processDeltaUpdate(context, entityDelta);
      }
    }

    updateCaches(context);
  }

  public void clearExistingEntities(FeedProvider feedProvider) {
    var systemId = feedProvider.getSystemId();
    var existingVehicles = vehicleCache.getAll();
    var vehiclesToRemove = existingVehicles
      .stream()
      .filter(v -> systemId.equals(v.getSystemId()))
      .toList();

    if (!vehiclesToRemove.isEmpty()) {
      logger.debug(
        "Removing {} existing vehicles for system {} due to null base",
        vehiclesToRemove.size(),
        systemId
      );

      var idsToRemove = vehiclesToRemove
        .stream()
        .map(Vehicle::getId)
        .collect(Collectors.toSet());
      var spatialIdsToRemove = vehiclesToRemove
        .stream()
        .map(v -> spatialIndexService.createVehicleIndexId(v, feedProvider))
        .collect(Collectors.toSet());

      vehicleCache.removeAll(idsToRemove);
      spatialIndex.removeAll(spatialIdsToRemove);
    }
  }

  private void processDeltaDelete(
    UpdateContext context,
    GBFSEntityDelta<GBFSVehicle> entityDelta
  ) {
    Vehicle currentVehicle = vehicleCache.get(entityDelta.entityId());
    context.vehicleIdsToRemove.add(entityDelta.entityId());
    if (currentVehicle != null) {
      var spatialIndexId = spatialIndexService.createVehicleIndexId(
        currentVehicle,
        context.feedProvider
      );
      context.spatialIndexIdsToRemove.add(spatialIndexId);
    } else {
      logger.debug(
        "Vehicle {} marked for deletion but not found in cache",
        entityDelta.entityId()
      );
    }
  }

  private void processDeltaCreate(
    UpdateContext context,
    GBFSEntityDelta<GBFSVehicle> entityDelta
  ) {
    final GBFSVehicle deltaVehicle = entityDelta.entity();
    if (vehicleFilter.test(deltaVehicle)) {
      Vehicle mappedVehicle = vehicleMapper.mapVehicle(
        deltaVehicle,
        deltaVehicle.getStationId() != null
          ? stationCache.get(deltaVehicle.getStationId())
          : null,
        context.feedProvider.getSystemId()
      );
      context.addedAndUpdatedVehicles.put(mappedVehicle.getId(), mappedVehicle);
      var spatialIndexId = spatialIndexService.createVehicleIndexId(
        mappedVehicle,
        context.feedProvider
      );
      context.spatialIndexUpdateMap.put(spatialIndexId, mappedVehicle);
    }
  }

  private void processDeltaUpdate(
    UpdateContext context,
    GBFSEntityDelta<GBFSVehicle> entityDelta
  ) {
    Vehicle currentVehicle = vehicleCache.get(entityDelta.entityId());

    if (currentVehicle != null) {
      context.spatialIndexIdsToRemove.add(
        spatialIndexService.createVehicleIndexId(currentVehicle, context.feedProvider)
      );

      final GBFSVehicle deltaVehicle = entityDelta.entity();
      Vehicle mappedVehicle = vehicleMapper.mapVehicle(
        deltaVehicle,
        deltaVehicle.getStationId() != null
          ? stationCache.get(deltaVehicle.getStationId())
          : null,
        context.feedProvider.getSystemId()
      );

      context.addedAndUpdatedVehicles.put(mappedVehicle.getId(), mappedVehicle);

      context.spatialIndexUpdateMap.put(
        spatialIndexService.createVehicleIndexId(mappedVehicle, context.feedProvider),
        mappedVehicle
      );
    } else {
      logger.debug(
        "Vehicle {} not found in cache during update - attempting creation in case filtering criteria now allows inclusion",
        entityDelta.entityId()
      );

      /*
       * Handle vehicles that were previously filtered out by VehicleFilter during CREATE.
       * These vehicles won't be in the cache, but may appear in subsequent delta updates.
       * We attempt to create them again since their eligibility for inclusion may have
       * changed (e.g., a vehicle that was previously at a station may now no longer be).
       */
      processDeltaCreate(context, entityDelta);
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

    if (!context.vehicleIdsToRemove.isEmpty()) {
      logger.debug(
        "Removing {} vehicles from vehicle cache",
        context.vehicleIdsToRemove.size()
      );
      vehicleCache.removeAll(new HashSet<>(context.vehicleIdsToRemove));
    }

    if (!context.addedAndUpdatedVehicles.isEmpty()) {
      logger.debug(
        "Adding/updating {} vehicles in vehicle cache",
        context.addedAndUpdatedVehicles.size()
      );
      vehicleCache.updateAll(context.addedAndUpdatedVehicles);
    }

    if (!context.spatialIndexUpdateMap.isEmpty()) {
      logger.debug(
        "Updating {} entries in spatial index",
        context.spatialIndexUpdateMap.size()
      );
      spatialIndex.addAll(context.spatialIndexUpdateMap);
    }

    metricsService.registerEntityCount(
      MetricsService.ENTITY_VEHICLE,
      vehicleCache.count()
    );
  }
}
