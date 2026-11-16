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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.entur.lamassu.cache.EntityCache;
import org.entur.lamassu.cache.StationSpatialIndex;
import org.entur.lamassu.cache.StationSpatialIndexId;
import org.entur.lamassu.cache.VehicleSpatialIndex;
import org.entur.lamassu.cache.VehicleSpatialIndexId;
import org.entur.lamassu.model.entities.Station;
import org.entur.lamassu.model.entities.Vehicle;
import org.entur.lamassu.model.provider.FeedProvider;
import org.entur.lamassu.service.FeedProviderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrphanedEntityCleanerTest {

  @Mock
  private EntityCache<Vehicle> vehicleCache;

  @Mock
  private EntityCache<Station> stationCache;

  @Mock
  private VehicleSpatialIndex vehicleSpatialIndex;

  @Mock
  private StationSpatialIndex stationSpatialIndex;

  @Mock
  private FeedProviderService feedProviderService;

  private OrphanedEntityCleaner cleaner;

  @BeforeEach
  void setUp() {
    cleaner =
      new OrphanedEntityCleaner(
        vehicleCache,
        stationCache,
        vehicleSpatialIndex,
        stationSpatialIndex,
        feedProviderService
      );
  }

  @Test
  void removesEntitiesAndIndexEntriesOfSystemsWithoutAnEnabledProvider() {
    providers(provider("live", true), provider("disabled", false));
    var liveVehicle = vehicle("v-live", "live");
    var disabledVehicle = vehicle("v-disabled", "disabled");
    var removedVehicle = vehicle("v-removed", "removed");
    var liveStation = station("s-live", "live");
    var disabledStation = station("s-disabled", "disabled");
    when(vehicleCache.getAll())
      .thenReturn(List.of(liveVehicle, disabledVehicle, removedVehicle));
    when(stationCache.getAll()).thenReturn(List.of(liveStation, disabledStation));
    var liveVehicleIndex = vehicleIndex("v-live", "live");
    var disabledVehicleIndex = vehicleIndex("v-disabled", "disabled");
    var removedVehicleIndex = vehicleIndex("v-removed", "removed");
    var liveStationIndex = stationIndex("s-live", "live");
    var disabledStationIndex = stationIndex("s-disabled", "disabled");
    when(vehicleSpatialIndex.getAll())
      .thenReturn(List.of(liveVehicleIndex, disabledVehicleIndex, removedVehicleIndex));
    when(stationSpatialIndex.getAll())
      .thenReturn(List.of(liveStationIndex, disabledStationIndex));

    var removed = cleaner.removeOrphans();

    verify(vehicleCache).removeAll(Set.of("v-disabled", "v-removed"));
    verify(stationCache).removeAll(Set.of("s-disabled"));
    verify(vehicleSpatialIndex)
      .removeAll(Set.of(disabledVehicleIndex, removedVehicleIndex));
    verify(stationSpatialIndex).removeAll(Set.of(disabledStationIndex));
    assertEquals(Map.of("disabled", 1, "removed", 1), removed.vehiclesBySystem());
    assertEquals(Map.of("disabled", 1), removed.stationsBySystem());
  }

  @Test
  void removesIndexEntriesLeftBehindWithoutAnyEntity() {
    providers(provider("live", true));
    when(vehicleCache.getAll()).thenReturn(List.of(vehicle("v-live", "live")));
    when(stationCache.getAll()).thenReturn(List.of());
    var leftover = vehicleIndex("v-gone", "disabled");
    when(vehicleSpatialIndex.getAll())
      .thenReturn(List.of(vehicleIndex("v-live", "live"), leftover));
    when(stationSpatialIndex.getAll()).thenReturn(List.of());

    cleaner.removeOrphans();

    verify(vehicleCache, never()).removeAll(any());
    verify(vehicleSpatialIndex).removeAll(Set.of(leftover));
  }

  @Test
  void findOrphansReportsWithoutRemovingAnything() {
    providers(provider("live", true), provider("disabled", false));
    when(vehicleCache.getAll())
      .thenReturn(
        List.of(
          vehicle("v1", "disabled"),
          vehicle("v2", "disabled"),
          vehicle("v3", "live")
        )
      );
    when(stationCache.getAll()).thenReturn(List.of());

    var orphans = cleaner.findOrphans();

    assertEquals(Map.of("disabled", 2), orphans.vehiclesBySystem());
    assertTrue(orphans.stationsBySystem().isEmpty());
    verify(vehicleCache, never()).removeAll(any());
    verify(vehicleSpatialIndex, never()).removeAll(any());
  }

  @Test
  void removesNothingWhenNoEnabledProvidersAreLoaded() {
    providers(provider("disabled", false));

    var removed = cleaner.removeOrphans();

    assertTrue(removed.isEmpty());
    verify(vehicleCache, never()).getAll();
    verify(vehicleCache, never()).removeAll(any());
    verify(stationCache, never()).removeAll(any());
    verify(vehicleSpatialIndex, never()).removeAll(any());
    verify(stationSpatialIndex, never()).removeAll(any());
  }

  @Test
  void leavesEntitiesWithoutASystemIdAlone() {
    providers(provider("live", true));
    when(vehicleCache.getAll()).thenReturn(List.of(vehicle("v-unknown", null)));
    when(stationCache.getAll()).thenReturn(List.of());
    when(vehicleSpatialIndex.getAll()).thenReturn(List.of());
    when(stationSpatialIndex.getAll()).thenReturn(List.of());

    var removed = cleaner.removeOrphans();

    assertTrue(removed.isEmpty());
    verify(vehicleCache, never()).removeAll(any());
  }

  private void providers(FeedProvider... providers) {
    when(feedProviderService.getFeedProviders()).thenReturn(List.of(providers));
  }

  private static FeedProvider provider(String systemId, boolean enabled) {
    var provider = new FeedProvider();
    provider.setSystemId(systemId);
    provider.setEnabled(enabled);
    return provider;
  }

  private static Vehicle vehicle(String id, String systemId) {
    var vehicle = new Vehicle();
    vehicle.setId(id);
    vehicle.setSystemId(systemId);
    return vehicle;
  }

  private static Station station(String id, String systemId) {
    var station = new Station();
    station.setId(id);
    station.setSystemId(systemId);
    return station;
  }

  private static VehicleSpatialIndexId vehicleIndex(String id, String systemId) {
    var indexId = new VehicleSpatialIndexId();
    indexId.setId(id);
    indexId.setSystemId(systemId);
    return indexId;
  }

  private static StationSpatialIndexId stationIndex(String id, String systemId) {
    var indexId = new StationSpatialIndexId();
    indexId.setId(id);
    indexId.setSystemId(systemId);
    return indexId;
  }
}
