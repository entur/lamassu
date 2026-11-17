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

package org.entur.lamassu.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.entur.lamassu.cache.EntityCache;
import org.entur.lamassu.cache.EntityReader;
import org.entur.lamassu.cache.StationSpatialIndex;
import org.entur.lamassu.cache.StationSpatialIndexId;
import org.entur.lamassu.cache.VehicleSpatialIndex;
import org.entur.lamassu.cache.VehicleSpatialIndexId;
import org.entur.lamassu.model.entities.Station;
import org.entur.lamassu.model.entities.System;
import org.entur.lamassu.model.entities.Vehicle;
import org.entur.lamassu.service.impl.GeoSearchServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.geo.GeoOrder;
import org.redisson.api.geo.GeoUnit;

@ExtendWith(MockitoExtension.class)
class GeoSearchServiceSystemFilterTest {

  @Mock
  private VehicleSpatialIndex vehicleSpatialIndex;

  @Mock
  private StationSpatialIndex stationSpatialIndex;

  @Mock
  private EntityCache<Vehicle> vehicleCache;

  @Mock
  private EntityCache<Station> stationCache;

  @Mock
  private EntityReader<System> systemReader;

  private GeoSearchServiceImpl service;

  @BeforeEach
  void setUp() {
    service =
      new GeoSearchServiceImpl(
        vehicleSpatialIndex,
        stationSpatialIndex,
        vehicleCache,
        stationCache,
        new ResolvableSystems(systemReader)
      );
    var found = new HashMap<String, System>();
    found.put("known", new System());
    when(systemReader.getAllAsMap(any())).thenReturn(found);
  }

  @Test
  void vehiclesWithoutAResolvableSystemDoNotTakeUpPlacesInTheCount() {
    // Nearest first, as the index returns them
    when(
      vehicleSpatialIndex.radius(
        anyDouble(),
        anyDouble(),
        anyDouble(),
        any(GeoUnit.class),
        any(GeoOrder.class)
      )
    )
      .thenReturn(
        List.of(vehicleIndexId("broken", "expired"), vehicleIndexId("working", "known"))
      );
    when(vehicleCache.getAll(Set.of("working"))).thenReturn(List.of(new Vehicle()));

    var vehicles = service.getVehiclesWithinRange(range(), vehicleFilter(1));

    assertEquals(1, vehicles.size());
  }

  @Test
  void stationsWithoutAResolvableSystemDoNotTakeUpPlacesInTheCount() {
    when(
      stationSpatialIndex.radius(
        anyDouble(),
        anyDouble(),
        anyDouble(),
        any(GeoUnit.class),
        any(GeoOrder.class)
      )
    )
      .thenReturn(
        List.of(stationIndexId("broken", "expired"), stationIndexId("working", "known"))
      );
    when(stationCache.getAll(Set.of("working"))).thenReturn(List.of(new Station()));

    var stations = service.getStationsWithinRange(range(), stationFilter(1));

    assertEquals(1, stations.size());
  }

  private static RangeQueryParameters range() {
    var range = new RangeQueryParameters();
    range.setLat(10.0);
    range.setLon(10.0);
    range.setRange(1000.0);
    return range;
  }

  private static VehicleFilterParameters vehicleFilter(int count) {
    return new VehicleFilterParameters(null, null, null, count, null, null, true, true);
  }

  private static StationFilterParameters stationFilter(int count) {
    return new StationFilterParameters(null, null, null, count, null, null);
  }

  private static VehicleSpatialIndexId vehicleIndexId(String id, String systemId) {
    var indexId = new VehicleSpatialIndexId();
    indexId.setId(id);
    indexId.setSystemId(systemId);
    return indexId;
  }

  private static StationSpatialIndexId stationIndexId(String id, String systemId) {
    var indexId = new StationSpatialIndexId();
    indexId.setId(id);
    indexId.setSystemId(systemId);
    return indexId;
  }
}
