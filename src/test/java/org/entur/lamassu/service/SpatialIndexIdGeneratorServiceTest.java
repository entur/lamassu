package org.entur.lamassu.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.entur.lamassu.cache.EntityCache;
import org.entur.lamassu.model.entities.FormFactor;
import org.entur.lamassu.model.entities.PropulsionType;
import org.entur.lamassu.model.entities.Station;
import org.entur.lamassu.model.entities.VehicleType;
import org.entur.lamassu.model.entities.VehicleTypeAvailability;
import org.entur.lamassu.model.provider.FeedProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SpatialIndexIdGeneratorServiceTest {

  @Mock
  private EntityCache<VehicleType> vehicleTypeCache;

  private SpatialIndexIdGeneratorService service;
  private FeedProvider feedProvider;

  @BeforeEach
  void setUp() {
    service = new SpatialIndexIdGeneratorService(vehicleTypeCache);

    feedProvider = new FeedProvider();
    feedProvider.setSystemId("test-system");
    feedProvider.setCodespace("test");
    feedProvider.setOperatorId("test-operator");
  }

  private static Station stationWithVehicleTypes(String... vehicleTypeIds) {
    var station = new Station();
    station.setId("station-1");
    station.setLat(59.9);
    station.setLon(10.7);

    var availabilities = new ArrayList<VehicleTypeAvailability>();
    for (var vehicleTypeId : vehicleTypeIds) {
      var availability = new VehicleTypeAvailability();
      availability.setVehicleTypeId(vehicleTypeId);
      availability.setCount(1);
      availabilities.add(availability);
    }
    station.setVehicleTypesAvailable(availabilities);
    return station;
  }

  private static VehicleType bicycle(String id) {
    var vehicleType = new VehicleType();
    vehicleType.setId(id);
    vehicleType.setFormFactor(FormFactor.BICYCLE);
    vehicleType.setPropulsionType(PropulsionType.HUMAN);
    return vehicleType;
  }

  @Test
  void shouldResolveFormFactorsWhenAllVehicleTypesAreKnown() {
    when(vehicleTypeCache.getAll(Set.of("bike"))).thenReturn(List.of(bicycle("bike")));

    var indexId = service.createStationIndexId(
      stationWithVehicleTypes("bike"),
      feedProvider
    );

    assertEquals(List.of(FormFactor.BICYCLE), indexId.getAvailableFormFactors());
    assertEquals(List.of(PropulsionType.HUMAN), indexId.getAvailablePropulsionTypes());
  }

  /**
   * EntityCacheImpl.getAllAsMap returns an empty map when the lookup times out, so a lookup
   * failure is indistinguishable from "this station has no vehicle types". Emitting a key
   * with empty form factors in that case produces an entry invisible to form-factor
   * filtering, and because the remove key and the add key are computed through two
   * independent calls, one can degrade while the other does not.
   */
  @Test
  void shouldThrowWhenVehicleTypeLookupReturnsNothing() {
    when(vehicleTypeCache.getAll(Set.of("bike"))).thenReturn(List.of());

    var station = stationWithVehicleTypes("bike");

    assertThrows(
      IllegalStateException.class,
      () -> service.createStationIndexId(station, feedProvider)
    );
  }

  @Test
  void shouldThrowWhenVehicleTypeLookupIsIncomplete() {
    when(vehicleTypeCache.getAll(Set.of("bike", "scooter")))
      .thenReturn(List.of(bicycle("bike")));

    var station = stationWithVehicleTypes("bike", "scooter");

    assertThrows(
      IllegalStateException.class,
      () -> service.createStationIndexId(station, feedProvider)
    );
  }

  @Test
  void shouldNotThrowWhenStationHasNoVehicleTypesAtAll() {
    var station = new Station();
    station.setId("station-1");

    var indexId = service.createStationIndexId(station, feedProvider);

    assertEquals(List.of(), indexId.getAvailableFormFactors());
    assertEquals(List.of(), indexId.getAvailablePropulsionTypes());
  }
}
