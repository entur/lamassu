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

package org.entur.lamassu.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.entur.lamassu.cache.EntityCache;
import org.entur.lamassu.cache.StationSpatialIndex;
import org.entur.lamassu.cache.StationSpatialIndexId;
import org.entur.lamassu.cache.VehicleSpatialIndex;
import org.entur.lamassu.cache.VehicleSpatialIndexId;
import org.entur.lamassu.config.feedprovider.FeedProviderConfig;
import org.entur.lamassu.leader.OrphanedEntityCleaner;
import org.entur.lamassu.model.entities.FormFactor;
import org.entur.lamassu.model.entities.PropulsionType;
import org.entur.lamassu.model.entities.Station;
import org.entur.lamassu.model.entities.Vehicle;
import org.entur.lamassu.model.provider.FeedProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Runs the orphan cleanup against the real caches and spatial index, since a mocked cache
 * says nothing about whether the removals actually reach Redis.
 */
class OrphanedEntityCleanerIntegrationTest extends AbstractIntegrationTestBase {

  private static final String DISABLED_SYSTEM_ID = "orphan-test-disabled";
  private static final String REMOVED_SYSTEM_ID = "orphan-test-removed";

  @Autowired
  private OrphanedEntityCleaner cleaner;

  @Autowired
  private EntityCache<Vehicle> vehicleCache;

  @Autowired
  private VehicleSpatialIndex vehicleSpatialIndex;

  @Autowired
  private EntityCache<Station> stationCache;

  @Autowired
  private StationSpatialIndex stationSpatialIndex;

  @Autowired
  private FeedProviderConfig feedProviderConfig;

  @Test
  void removesVehiclesAndIndexEntriesOfDisabledAndRemovedSystemsOnly() {
    var disabledProvider = new FeedProvider();
    disabledProvider.setSystemId(DISABLED_SYSTEM_ID);
    disabledProvider.setUrl("http://localhost:8888/orphan-test/gbfs");
    disabledProvider.setLanguage("en");
    disabledProvider.setEnabled(false);
    feedProviderConfig.addProvider(disabledProvider);

    var enabledSystemId = feedProviderConfig
      .getProviders()
      .stream()
      .filter(provider -> Boolean.TRUE.equals(provider.getEnabled()))
      .map(FeedProvider::getSystemId)
      .findFirst()
      .orElseThrow();

    var live = vehicle("orphan-test-live", enabledSystemId);
    var disabled = vehicle("orphan-test-disabled", DISABLED_SYSTEM_ID);
    var removed = vehicle("orphan-test-removed", REMOVED_SYSTEM_ID);
    for (var vehicle : new Vehicle[] { live, disabled, removed }) {
      vehicleCache.updateAll(Map.of(vehicle.getId(), vehicle));
      vehicleSpatialIndex.addAll(Map.of(indexId(vehicle), vehicle));
    }

    var liveStation = station("orphan-test-station-live", enabledSystemId);
    var disabledStation = station("orphan-test-station-disabled", DISABLED_SYSTEM_ID);
    for (var station : new Station[] { liveStation, disabledStation }) {
      stationCache.updateAll(Map.of(station.getId(), station));
      stationSpatialIndex.addAll(Map.of(stationIndexId(station), station));
    }

    try {
      var orphans = cleaner.findOrphans();
      assertEquals(1, orphans.vehiclesBySystem().get(DISABLED_SYSTEM_ID));
      assertEquals(1, orphans.vehiclesBySystem().get(REMOVED_SYSTEM_ID));
      assertNull(orphans.vehiclesBySystem().get(enabledSystemId));
      assertTrue(vehicleCache.hasKey(disabled.getId()), "finding must not remove");

      cleaner.removeOrphans();

      assertNull(vehicleCache.get(disabled.getId()));
      assertNull(vehicleCache.get(removed.getId()));
      assertEquals(live.getId(), vehicleCache.get(live.getId()).getId());

      var indexedIds = vehicleSpatialIndex
        .getAll()
        .stream()
        .map(VehicleSpatialIndexId::getId)
        .toList();
      assertFalse(indexedIds.contains(disabled.getId()));
      assertFalse(indexedIds.contains(removed.getId()));
      assertTrue(indexedIds.contains(live.getId()));

      assertNull(stationCache.get(disabledStation.getId()));
      assertEquals(liveStation.getId(), stationCache.get(liveStation.getId()).getId());
      var indexedStationIds = stationSpatialIndex
        .getAll()
        .stream()
        .map(StationSpatialIndexId::getId)
        .toList();
      assertFalse(indexedStationIds.contains(disabledStation.getId()));
      assertTrue(indexedStationIds.contains(liveStation.getId()));
    } finally {
      vehicleCache.removeAll(Set.of(live.getId()));
      vehicleSpatialIndex.removeAll(Set.of(indexId(live)));
      stationCache.removeAll(Set.of(liveStation.getId()));
      stationSpatialIndex.removeAll(Set.of(stationIndexId(liveStation)));
      feedProviderConfig.deleteProvider(DISABLED_SYSTEM_ID);
    }
  }

  private static Vehicle vehicle(String id, String systemId) {
    var vehicle = new Vehicle();
    vehicle.setId(id);
    vehicle.setSystemId(systemId);
    vehicle.setLat(59.91);
    vehicle.setLon(10.75);
    return vehicle;
  }

  private static Station station(String id, String systemId) {
    var station = new Station();
    station.setId(id);
    station.setSystemId(systemId);
    station.setLat(59.91);
    station.setLon(10.75);
    return station;
  }

  private static StationSpatialIndexId stationIndexId(Station station) {
    var indexId = new StationSpatialIndexId();
    indexId.setId(station.getId());
    indexId.setSystemId(station.getSystemId());
    indexId.setAvailableFormFactors(List.of(FormFactor.SCOOTER));
    indexId.setAvailablePropulsionTypes(List.of(PropulsionType.ELECTRIC));
    return indexId;
  }

  private static VehicleSpatialIndexId indexId(Vehicle vehicle) {
    var indexId = new VehicleSpatialIndexId();
    indexId.setId(vehicle.getId());
    indexId.setSystemId(vehicle.getSystemId());
    return indexId;
  }
}
