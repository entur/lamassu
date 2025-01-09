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

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.entur.gbfs.loader.v3.GbfsV3Delivery;
import org.entur.lamassu.cache.EntityCache;
import org.entur.lamassu.cache.VehicleSpatialIndex;
import org.entur.lamassu.cache.VehicleSpatialIndexId;
import org.entur.lamassu.mapper.entitymapper.VehicleMapper;
import org.entur.lamassu.metrics.MetricsService;
import org.entur.lamassu.model.entities.Vehicle;
import org.entur.lamassu.model.provider.FeedProvider;
import org.entur.lamassu.service.SpatialIndexIdGeneratorService;
import org.entur.lamassu.util.CacheUtil;
import org.mobilitydata.gbfs.v3_0.vehicle_status.GBFSVehicle;
import org.mobilitydata.gbfs.v3_0.vehicle_status.GBFSVehicleStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class VehiclesUpdater {

  private final EntityCache<Vehicle> vehicleCache;
  private final VehicleSpatialIndex spatialIndex;
  private final VehicleMapper vehicleMapper;
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private final MetricsService metricsService;
  private final SpatialIndexIdGeneratorService spatialIndexService;
  private final VehicleFilter vehicleFilter;

  @Value("${org.entur.lamassu.vehicleEntityCacheMinimumTtl:30}")
  private Integer vehicleEntityCacheMinimumTtl;

  @Value("${org.entur.lamassu.vehicleEntityCacheMaximumTtl:300}")
  private Integer vehicleEntityCacheMaximumTtl;

  @Autowired
  public VehiclesUpdater(
    EntityCache<Vehicle> vehicleCache,
    VehicleSpatialIndex spatialIndex,
    VehicleMapper vehicleMapper,
    MetricsService metricsService,
    SpatialIndexIdGeneratorService spatialIndexService,
    VehicleFilter vehicleFilter
  ) {
    this.vehicleCache = vehicleCache;
    this.spatialIndex = spatialIndex;
    this.vehicleMapper = vehicleMapper;
    this.metricsService = metricsService;
    this.spatialIndexService = spatialIndexService;
    this.vehicleFilter = vehicleFilter;
  }

  public void addOrUpdateVehicles(
    FeedProvider feedProvider,
    GbfsV3Delivery delivery,
    GbfsV3Delivery oldDelivery
  ) {
    GBFSVehicleStatus vehicleStatusFeed = delivery.vehicleStatus();
    GBFSVehicleStatus oldFreeBikeStatusFeed = oldDelivery.vehicleStatus();

    var vehicleIds = vehicleStatusFeed
      .getData()
      .getVehicles()
      .stream()
      .map(GBFSVehicle::getVehicleId)
      .collect(Collectors.toSet());

    Set<String> vehicleIdsToRemove = null;

    if (oldFreeBikeStatusFeed != null && oldFreeBikeStatusFeed.getData() != null) {
      vehicleIdsToRemove =
        oldFreeBikeStatusFeed
          .getData()
          .getVehicles()
          .stream()
          .map(GBFSVehicle::getVehicleId)
          .collect(Collectors.toSet());

      // Find vehicle ids in old feed not present in new feed
      vehicleIdsToRemove.removeAll(vehicleIds);
      logger.trace(
        "Found {} vehicleIds to remove from old free_bike_status feed: {}",
        vehicleIdsToRemove.size(),
        oldFreeBikeStatusFeed
      );

      // Add vehicle ids that are staged for removal to the set of vehicle ids that will be used to
      // fetch current vehicles from cache
      vehicleIds.addAll(vehicleIdsToRemove);
    }

    if (vehicleIdsToRemove == null) {
      vehicleIdsToRemove = new HashSet<>(vehicleIds);
      logger.debug(
        "Old free_bike_status feed was not available or had no data. As a workaround, removing all vehicles for provider {}",
        feedProvider.getSystemId()
      );
    }

    var currentVehicles = vehicleCache.getAllAsMap(new HashSet<>(vehicleIds));

    var vehicleList = vehicleStatusFeed
      .getData()
      .getVehicles()
      .stream()
      .filter(vehicleFilter)
      .map(vehicle -> vehicleMapper.mapVehicle(vehicle, feedProvider.getSystemId()))
      .toList();

    var duplicateVehicles = vehicleList
      .stream()
      .filter(i -> Collections.frequency(vehicleList, i) > 1)
      .collect(Collectors.toSet());

    if (!duplicateVehicles.isEmpty() && logger.isWarnEnabled()) {
      logger.warn(
        "Removed duplicate vehicles with ids: {}",
        duplicateVehicles.stream().map(Vehicle::getId).collect(Collectors.joining(","))
      );
    }

    var vehicles = vehicleList
      .stream()
      .filter(i -> Collections.frequency(vehicleList, i) == 1)
      .collect(Collectors.toMap(Vehicle::getId, v -> v));

    Set<VehicleSpatialIndexId> spatialIndicesToRemove = new java.util.HashSet<>(Set.of());
    Map<VehicleSpatialIndexId, Vehicle> spatialIndexUpdateMap = new java.util.HashMap<>(
      Map.of()
    );

    vehicles.forEach((key, vehicle) -> {
      var spatialIndexId = spatialIndexService.createVehicleIndexId(
        vehicle,
        feedProvider
      );
      var previousVehicle = currentVehicles.get(key);

      if (previousVehicle != null) {
        var oldSpatialIndexId = spatialIndexService.createVehicleIndexId(
          previousVehicle,
          feedProvider
        );
        if (!oldSpatialIndexId.equals(spatialIndexId)) {
          spatialIndicesToRemove.add(oldSpatialIndexId);
        }
      }
      spatialIndexUpdateMap.put(spatialIndexId, vehicle);
    });

    spatialIndicesToRemove.addAll(
      vehicleIdsToRemove
        .stream()
        .filter(currentVehicles::containsKey)
        .map(id ->
          spatialIndexService.createVehicleIndexId(currentVehicles.get(id), feedProvider)
        )
        .collect(Collectors.toSet())
    );

    if (!spatialIndicesToRemove.isEmpty()) {
      logger.debug(
        "Removing {} stale entries in spatial index",
        spatialIndicesToRemove.size()
      );
      spatialIndex.removeAll(spatialIndicesToRemove);
    }

    if (!vehicleIdsToRemove.isEmpty()) {
      logger.debug("Removing {} vehicles from vehicle cache", vehicleIdsToRemove.size());
      vehicleCache.removeAll(new HashSet<>(vehicleIdsToRemove));
    }

    if (!vehicles.isEmpty()) {
      logger.debug("Adding/updating {} vehicles in vehicle cache", vehicles.size());
      var lastUpdated = vehicleStatusFeed.getLastUpdated();
      var ttl = vehicleStatusFeed.getTtl();
      vehicleCache.updateAll(
        vehicles,
        CacheUtil.getTtl(
          (int) (lastUpdated.getTime() / 1000),
          ttl,
          vehicleEntityCacheMinimumTtl,
          vehicleEntityCacheMaximumTtl
        ),
        TimeUnit.SECONDS
      );
    }

    if (!spatialIndexUpdateMap.isEmpty()) {
      logger.debug("Updating {} entries in spatial index", spatialIndexUpdateMap.size());
      spatialIndex.addAll(spatialIndexUpdateMap);
    }

    metricsService.registerEntityCount(
      MetricsService.ENTITY_VEHICLE,
      vehicleCache.count()
    );
  }
}
