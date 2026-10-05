package org.entur.lamassu.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
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
import org.entur.lamassu.metrics.MetricsService;
import org.entur.lamassu.model.entities.Station;
import org.entur.lamassu.model.entities.Vehicle;
import org.entur.lamassu.model.provider.FeedProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SpatialIndexReconcilerTest {

  private static final String SYSTEM_ID = "test-system";

  @Mock
  private EntityCache<Station> stationCache;

  @Mock
  private EntityCache<Vehicle> vehicleCache;

  @Mock
  private StationSpatialIndex stationSpatialIndex;

  @Mock
  private VehicleSpatialIndex vehicleSpatialIndex;

  @Mock
  private SpatialIndexIdGeneratorService spatialIndexIdGeneratorService;

  @Mock
  private FeedProviderService feedProviderService;

  @Mock
  private MetricsService metricsService;

  private FeedProvider feedProvider;
  private SpatialIndexReconciler reconciler;

  @BeforeEach
  void setUp() {
    feedProvider = new FeedProvider();
    feedProvider.setSystemId(SYSTEM_ID);
    feedProvider.setCodespace("test");
    feedProvider.setOperatorId("test-operator");

    when(feedProviderService.getFeedProviderBySystemId(SYSTEM_ID))
      .thenReturn(feedProvider);

    // Default to empty on both sides so each test only has to set up what it cares about.
    when(stationCache.getKeys()).thenReturn(Set.of());
    when(vehicleCache.getKeys()).thenReturn(Set.of());
    when(stationSpatialIndex.getAll()).thenReturn(List.of());
    when(vehicleSpatialIndex.getAll()).thenReturn(List.of());

    reconciler =
      new SpatialIndexReconciler(
        stationCache,
        vehicleCache,
        stationSpatialIndex,
        vehicleSpatialIndex,
        spatialIndexIdGeneratorService,
        feedProviderService,
        metricsService
      );
  }

  private static Station station(String id) {
    var station = new Station();
    station.setId(id);
    station.setSystemId(SYSTEM_ID);
    station.setLat(59.9);
    station.setLon(10.7);
    return station;
  }

  private static Vehicle vehicle(String id) {
    var vehicle = new Vehicle();
    vehicle.setId(id);
    vehicle.setSystemId(SYSTEM_ID);
    vehicle.setLat(59.9);
    vehicle.setLon(10.7);
    return vehicle;
  }

  private static StationSpatialIndexId stationIndexId(String id) {
    var indexId = new StationSpatialIndexId();
    indexId.setId(id);
    indexId.setCodespace("test");
    indexId.setSystemId(SYSTEM_ID);
    indexId.setOperatorId("test-operator");
    indexId.setAvailableFormFactors(List.of());
    indexId.setAvailablePropulsionTypes(List.of());
    return indexId;
  }

  private static VehicleSpatialIndexId vehicleIndexId(String id) {
    var indexId = new VehicleSpatialIndexId();
    indexId.setId(id);
    indexId.setCodespace("test");
    indexId.setSystemId(SYSTEM_ID);
    indexId.setOperatorId("test-operator");
    return indexId;
  }

  @SuppressWarnings("unchecked")
  private Map<StationSpatialIndexId, Station> captureStationAdds() {
    var captor = ArgumentCaptor.forClass(Map.class);
    verify(stationSpatialIndex).addAll(captor.capture());
    return captor.getValue();
  }

  @SuppressWarnings("unchecked")
  private Map<VehicleSpatialIndexId, Vehicle> captureVehicleAdds() {
    var captor = ArgumentCaptor.forClass(Map.class);
    verify(vehicleSpatialIndex).addAll(captor.capture());
    return captor.getValue();
  }

  @Test
  void reAddsStationsPresentInTheCacheButMissingFromTheIndex() {
    var stranded = station("stranded");
    when(stationCache.getKeys()).thenReturn(Set.of("indexed", "stranded"));
    when(stationSpatialIndex.getAll()).thenReturn(List.of(stationIndexId("indexed")));
    when(stationCache.getAllAsMap(Set.of("stranded")))
      .thenReturn(Map.of("stranded", stranded));
    when(spatialIndexIdGeneratorService.createStationIndexId(stranded, feedProvider))
      .thenReturn(stationIndexId("stranded"));

    assertEquals(1, reconciler.reconcileStations());

    assertEquals(Map.of(stationIndexId("stranded"), stranded), captureStationAdds());
  }

  @Test
  void leavesTheIndexAloneWhenEveryStationIsIndexed() {
    when(stationCache.getKeys()).thenReturn(Set.of("indexed"));
    when(stationSpatialIndex.getAll()).thenReturn(List.of(stationIndexId("indexed")));

    assertEquals(0, reconciler.reconcileStations());

    verify(stationSpatialIndex, never()).addAll(any());
    verify(stationCache, never()).getAllAsMap(anySet());
  }

  /**
   * A station whose vehicle types cannot be resolved produces no usable index id. Skipping it
   * must not stop the rest of the batch from being repaired.
   */
  @Test
  void skipsAStationWhoseIndexIdCannotBeGenerated() {
    var degraded = station("degraded");
    var repairable = station("repairable");
    when(stationCache.getKeys()).thenReturn(Set.of("degraded", "repairable"));
    when(stationCache.getAllAsMap(Set.of("degraded", "repairable")))
      .thenReturn(Map.of("degraded", degraded, "repairable", repairable));
    when(spatialIndexIdGeneratorService.createStationIndexId(degraded, feedProvider))
      .thenThrow(new IllegalStateException("unresolved vehicle types"));
    when(spatialIndexIdGeneratorService.createStationIndexId(repairable, feedProvider))
      .thenReturn(stationIndexId("repairable"));

    assertEquals(1, reconciler.reconcileStations());

    assertEquals(Map.of(stationIndexId("repairable"), repairable), captureStationAdds());
  }

  @Test
  void skipsAnEntityWhoseFeedProviderIsUnknown() {
    var orphaned = station("orphaned");
    orphaned.setSystemId("retired-system");
    when(stationCache.getKeys()).thenReturn(Set.of("orphaned"));
    when(stationCache.getAllAsMap(Set.of("orphaned")))
      .thenReturn(Map.of("orphaned", orphaned));
    when(feedProviderService.getFeedProviderBySystemId("retired-system"))
      .thenReturn(null);

    assertEquals(0, reconciler.reconcileStations());

    verify(stationSpatialIndex, never()).addAll(any());
  }

  /**
   * The key scan and the re-read are two separate round trips. A station deleted in between
   * is gone from the cache by the time we look it up, and must not be put back into the
   * index - that would leave an entry pointing at an entity that no longer exists.
   */
  @Test
  void doesNotResurrectAStationDeletedBetweenTheKeyScanAndTheReAdd() {
    when(stationCache.getKeys()).thenReturn(Set.of("deleted"));
    when(stationCache.getAllAsMap(Set.of("deleted"))).thenReturn(Map.of());

    assertEquals(0, reconciler.reconcileStations());

    verify(stationSpatialIndex, never()).addAll(any());
  }

  @Test
  void reAddsVehiclesPresentInTheCacheButMissingFromTheIndex() {
    var stranded = vehicle("stranded");
    when(vehicleCache.getKeys()).thenReturn(Set.of("indexed", "stranded"));
    when(vehicleSpatialIndex.getAll()).thenReturn(List.of(vehicleIndexId("indexed")));
    when(vehicleCache.getAllAsMap(Set.of("stranded")))
      .thenReturn(Map.of("stranded", stranded));
    when(spatialIndexIdGeneratorService.createVehicleIndexId(stranded, feedProvider))
      .thenReturn(vehicleIndexId("stranded"));

    assertEquals(1, reconciler.reconcileVehicles());

    assertEquals(Map.of(vehicleIndexId("stranded"), stranded), captureVehicleAdds());
  }

  /**
   * The gauge has to be written on every run, including with zero, or a resolved problem
   * would stay visible at its last non-zero value forever.
   */
  @Test
  void reportsTheReAddedCountOnEveryRunIncludingZero() {
    reconciler.reconcile();

    verify(metricsService)
      .registerSpatialIndexReconciledCount(MetricsService.ENTITY_STATION, 0);
    verify(metricsService)
      .registerSpatialIndexReconciledCount(MetricsService.ENTITY_VEHICLE, 0);
  }

  @Test
  void reconcilesVehiclesEvenWhenStationReconciliationFails() {
    when(stationSpatialIndex.getAll()).thenThrow(new RuntimeException("redis is down"));

    reconciler.reconcile();

    verify(metricsService)
      .registerSpatialIndexReconciledCount(MetricsService.ENTITY_VEHICLE, 0);
  }
}
