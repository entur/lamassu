package org.entur.lamassu.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import org.entur.lamassu.cache.EntityCache;
import org.entur.lamassu.cache.SpatialIndex;
import org.entur.lamassu.cache.SpatialIndexId;
import org.entur.lamassu.cache.StationSpatialIndex;
import org.entur.lamassu.cache.VehicleSpatialIndex;
import org.entur.lamassu.metrics.MetricsService;
import org.entur.lamassu.model.entities.LocationEntity;
import org.entur.lamassu.model.entities.Station;
import org.entur.lamassu.model.entities.SystemEntity;
import org.entur.lamassu.model.entities.Vehicle;
import org.entur.lamassu.model.provider.FeedProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Puts back entities that are present in an entity cache but missing from the corresponding
 * spatial index.
 *
 * <p>The spatial index is a cache continuously derived from upstream GBFS feeds, so the
 * correctness model that fits it is convergence rather than transactional isolation. The
 * write path can lose an entry for any number of reasons - a bug, a partial failure, an
 * eviction, manual Redis surgery - and nothing else in the system repairs the result. An
 * entity missing from the index is invisible to every geo query while still being returned
 * by id lookups and the GBFS feeds, and it is re-indexed only when its own status next
 * changes. Quiet entities, which are exactly the ones natural recovery cannot reach, can
 * stay missing indefinitely.
 *
 * <p>This is the counterpart to
 * {@link GeoSearchService#removeVehicleSpatialIndexOrphans()}, which covers the opposite
 * direction (index entries whose entity is gone) and only for vehicles.
 */
@Service
public class SpatialIndexReconciler {

  /**
   * How many ids to name in the log line. Enough to chase a specific entity through the
   * feeds, bounded so that a large backlog does not flood the log.
   */
  private static final int MAX_LOGGED_IDS = 20;

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  private final EntityCache<Station> stationCache;
  private final EntityCache<Vehicle> vehicleCache;
  private final StationSpatialIndex stationSpatialIndex;
  private final VehicleSpatialIndex vehicleSpatialIndex;
  private final SpatialIndexIdGeneratorService spatialIndexIdGeneratorService;
  private final FeedProviderService feedProviderService;
  private final MetricsService metricsService;

  @Autowired
  public SpatialIndexReconciler(
    EntityCache<Station> stationCache,
    EntityCache<Vehicle> vehicleCache,
    StationSpatialIndex stationSpatialIndex,
    VehicleSpatialIndex vehicleSpatialIndex,
    SpatialIndexIdGeneratorService spatialIndexIdGeneratorService,
    FeedProviderService feedProviderService,
    MetricsService metricsService
  ) {
    this.stationCache = stationCache;
    this.vehicleCache = vehicleCache;
    this.stationSpatialIndex = stationSpatialIndex;
    this.vehicleSpatialIndex = vehicleSpatialIndex;
    this.spatialIndexIdGeneratorService = spatialIndexIdGeneratorService;
    this.feedProviderService = feedProviderService;
    this.metricsService = metricsService;
  }

  /**
   * Reconciles both entity types. Each type is isolated, so a failure reconciling one does
   * not stop the other.
   */
  public void reconcile() {
    try {
      reconcileStations();
    } catch (RuntimeException e) {
      logger.warn("Failed reconciling the station spatial index", e);
    }

    try {
      reconcileVehicles();
    } catch (RuntimeException e) {
      logger.warn("Failed reconciling the vehicle spatial index", e);
    }
  }

  public int reconcileStations() {
    return reconcile(
      MetricsService.ENTITY_STATION,
      stationCache,
      stationSpatialIndex,
      spatialIndexIdGeneratorService::createStationIndexId
    );
  }

  public int reconcileVehicles() {
    return reconcile(
      MetricsService.ENTITY_VEHICLE,
      vehicleCache,
      vehicleSpatialIndex,
      spatialIndexIdGeneratorService::createVehicleIndexId
    );
  }

  /**
   * @return the number of entities put back into the index
   */
  private <
    S extends SpatialIndexId, T extends LocationEntity & SystemEntity
  > int reconcile(
    String entityType,
    EntityCache<T> cache,
    SpatialIndex<S, T> spatialIndex,
    BiFunction<T, FeedProvider, S> indexIdGenerator
  ) {
    Set<String> indexedIds = spatialIndex
      .getAll()
      .stream()
      .filter(Objects::nonNull)
      .map(SpatialIndexId::getId)
      .collect(Collectors.toSet());

    // Reading the full member set once and diffing against the cache keys keeps this to two
    // round trips, rather than one membership check per entity.
    Set<String> missingKeys = cache
      .getKeys()
      .stream()
      .filter(key -> !indexedIds.contains(key))
      .collect(Collectors.toSet());

    if (missingKeys.isEmpty()) {
      metricsService.registerSpatialIndexReconciledCount(entityType, 0);
      return 0;
    }

    // Re-read the entities rather than trusting the key scan. The key scan and this read are
    // separate round trips, and the feed updater runs concurrently: an entity deleted in
    // between is gone by now, and must not be put back into the index.
    Map<String, T> entities = cache.getAllAsMap(missingKeys);

    Map<S, T> toAdd = new HashMap<>();
    for (T entity : entities.values()) {
      var feedProvider = feedProviderService.getFeedProviderBySystemId(
        entity.getSystemId()
      );
      if (feedProvider == null) {
        logger.warn(
          "Not reconciling {} {} into the spatial index: no feed provider for systemId={}",
          entityType,
          entity.getId(),
          entity.getSystemId()
        );
        continue;
      }
      try {
        toAdd.put(indexIdGenerator.apply(entity, feedProvider), entity);
      } catch (RuntimeException e) {
        logger.warn(
          "Not reconciling {} {} into the spatial index: its index id could not be generated",
          entityType,
          entity.getId(),
          e
        );
      }
    }

    if (!toAdd.isEmpty()) {
      spatialIndex.addAll(toAdd);
      logger.info(
        "Re-added {} {} entries that were missing from the spatial index (up to {} shown): {}",
        toAdd.size(),
        entityType,
        MAX_LOGGED_IDS,
        describeIds(toAdd.keySet())
      );
    }

    metricsService.registerSpatialIndexReconciledCount(entityType, toAdd.size());
    return toAdd.size();
  }

  private String describeIds(Set<? extends SpatialIndexId> indexIds) {
    return indexIds
      .stream()
      .map(SpatialIndexId::getId)
      .sorted()
      .limit(MAX_LOGGED_IDS)
      .collect(Collectors.joining(", "));
  }
}
