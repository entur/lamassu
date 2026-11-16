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

package org.entur.lamassu.leader;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.entur.lamassu.cache.AbstractSpatialIndexId;
import org.entur.lamassu.cache.EntityCache;
import org.entur.lamassu.cache.SpatialIndex;
import org.entur.lamassu.cache.SpatialIndexId;
import org.entur.lamassu.cache.StationSpatialIndex;
import org.entur.lamassu.cache.VehicleSpatialIndex;
import org.entur.lamassu.model.entities.Entity;
import org.entur.lamassu.model.entities.Station;
import org.entur.lamassu.model.entities.Vehicle;
import org.entur.lamassu.model.provider.FeedProvider;
import org.entur.lamassu.service.FeedProviderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Removes vehicles and stations whose system no longer has an enabled feed provider.
 *
 * <p>Vehicles and stations are cached without a TTL, while the system and vehicle type
 * entries they refer to expire. A provider that is disabled or removed without a clean
 * stop therefore leaves entities behind that nothing can resolve a system or vehicle type
 * for. {@link CacheCleanupService} finds leftovers by walking the system cache, which no
 * longer helps once the system entry has expired, so this finds them from the entities
 * themselves. Spatial index keys are removed by system id rather than rebuilt from the
 * entities, since building a key needs the vehicle type that is already gone.
 */
@Service
public class OrphanedEntityCleaner {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  private final EntityCache<Vehicle> vehicleCache;
  private final EntityCache<Station> stationCache;
  private final VehicleSpatialIndex vehicleSpatialIndex;
  private final StationSpatialIndex stationSpatialIndex;
  private final FeedProviderService feedProviderService;

  /**
   * Orphaned entity counts per system id.
   */
  public record OrphanedEntities(
    Map<String, Integer> vehiclesBySystem,
    Map<String, Integer> stationsBySystem
  ) {
    public boolean isEmpty() {
      return vehiclesBySystem.isEmpty() && stationsBySystem.isEmpty();
    }
  }

  @Autowired
  public OrphanedEntityCleaner(
    EntityCache<Vehicle> vehicleCache,
    EntityCache<Station> stationCache,
    VehicleSpatialIndex vehicleSpatialIndex,
    StationSpatialIndex stationSpatialIndex,
    FeedProviderService feedProviderService
  ) {
    this.vehicleCache = vehicleCache;
    this.stationCache = stationCache;
    this.vehicleSpatialIndex = vehicleSpatialIndex;
    this.stationSpatialIndex = stationSpatialIndex;
    this.feedProviderService = feedProviderService;
  }

  /**
   * Reports the orphaned entities without removing anything.
   */
  public OrphanedEntities findOrphans() {
    var enabledSystemIds = enabledSystemIds();
    if (enabledSystemIds.isEmpty()) {
      logger.warn("No enabled feed providers loaded, not looking for orphaned entities");
      return new OrphanedEntities(Map.of(), Map.of());
    }
    return new OrphanedEntities(
      countBySystem(vehicleCache.getAll(), Vehicle::getSystemId, enabledSystemIds),
      countBySystem(stationCache.getAll(), Station::getSystemId, enabledSystemIds)
    );
  }

  /**
   * Removes the orphaned vehicles and stations, and their spatial index entries.
   *
   * <p>Does nothing when no enabled feed providers are loaded, so that a failed or empty
   * configuration load can never be mistaken for "every system was removed".
   */
  public OrphanedEntities removeOrphans() {
    var enabledSystemIds = enabledSystemIds();
    if (enabledSystemIds.isEmpty()) {
      logger.warn("No enabled feed providers loaded, not removing orphaned entities");
      return new OrphanedEntities(Map.of(), Map.of());
    }

    var vehiclesBySystem = removeOrphans(
      "vehicle",
      vehicleCache,
      Vehicle::getId,
      Vehicle::getSystemId,
      vehicleSpatialIndex,
      enabledSystemIds
    );
    var stationsBySystem = removeOrphans(
      "station",
      stationCache,
      Station::getId,
      Station::getSystemId,
      stationSpatialIndex,
      enabledSystemIds
    );
    return new OrphanedEntities(vehiclesBySystem, stationsBySystem);
  }

  private <
    T extends Entity, S extends AbstractSpatialIndexId & SpatialIndexId
  > Map<String, Integer> removeOrphans(
    String entityName,
    EntityCache<T> cache,
    Function<T, String> idOf,
    Function<T, String> systemIdOf,
    SpatialIndex<S, ?> spatialIndex,
    Set<String> enabledSystemIds
  ) {
    var all = cache.getAll();
    var orphans = all
      .stream()
      .filter(entity -> isOrphan(systemIdOf.apply(entity), enabledSystemIds))
      .toList();
    logger.info(
      "Checked {} {}s against {} enabled feed providers, {} belong to other systems",
      all.size(),
      entityName,
      enabledSystemIds.size(),
      orphans.size()
    );

    if (!orphans.isEmpty()) {
      cache.removeAll(orphans.stream().map(idOf).collect(Collectors.toSet()));
    }

    // Looked up after the entities are removed, so index entries that were already left
    // behind by an earlier failed cleanup are found too.
    Set<S> orphanedIndexIds = spatialIndex
      .getAll()
      .stream()
      .filter(indexId -> indexId != null)
      .filter(indexId -> isOrphan(indexId.getSystemId(), enabledSystemIds))
      .collect(Collectors.toSet());
    if (!orphanedIndexIds.isEmpty()) {
      spatialIndex.removeAll(orphanedIndexIds);
    }

    var counts = countBySystem(orphans, systemIdOf, enabledSystemIds);
    counts.forEach((systemId, count) ->
      logger.info(
        "Removed {} orphaned {}s for system {}, which has no enabled feed provider",
        count,
        entityName,
        systemId
      )
    );
    if (!orphanedIndexIds.isEmpty()) {
      logger.info(
        "Removed {} orphaned {} spatial index entries",
        orphanedIndexIds.size(),
        entityName
      );
    }
    return counts;
  }

  private static boolean isOrphan(String systemId, Set<String> enabledSystemIds) {
    // An entity without a system id can't be attributed to any provider, so it is left alone.
    return systemId != null && !enabledSystemIds.contains(systemId);
  }

  private <T> Map<String, Integer> countBySystem(
    Collection<T> entities,
    Function<T, String> systemIdOf,
    Set<String> enabledSystemIds
  ) {
    return entities
      .stream()
      .map(systemIdOf)
      .filter(systemId -> isOrphan(systemId, enabledSystemIds))
      .collect(
        Collectors.toMap(systemId -> systemId, systemId -> 1, Integer::sum, TreeMap::new)
      );
  }

  private Set<String> enabledSystemIds() {
    return feedProviderService
      .getFeedProviders()
      .stream()
      .filter(provider -> Boolean.TRUE.equals(provider.getEnabled()))
      .map(FeedProvider::getSystemId)
      .collect(Collectors.toSet());
  }
}
