package org.entur.lamassu.leader.entityupdater;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.TimeUnit;
import org.entur.lamassu.cache.EntityCache;
import org.entur.lamassu.cache.VehicleSpatialIndex;
import org.entur.lamassu.cache.VehicleSpatialIndexId;
import org.entur.lamassu.delta.DeltaType;
import org.entur.lamassu.delta.GBFSEntityDelta;
import org.entur.lamassu.delta.GBFSFileDelta;
import org.entur.lamassu.mapper.entitymapper.RentalUrisMapper;
import org.entur.lamassu.mapper.entitymapper.VehicleMapper;
import org.entur.lamassu.mapper.entitymapper.VehicleMergeMapper;
import org.entur.lamassu.mapper.entitymapper.VehicleMergeMapperImpl;
import org.entur.lamassu.metrics.MetricsService;
import org.entur.lamassu.model.entities.Vehicle;
import org.entur.lamassu.model.provider.FeedProvider;
import org.entur.lamassu.service.SpatialIndexIdGeneratorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mobilitydata.gbfs.v3_0.vehicle_status.GBFSVehicle;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class VehiclesUpdaterTest {

  @Mock
  private EntityCache<Vehicle> vehicleCache;

  @Mock
  private VehicleSpatialIndex spatialIndex;

  @Mock
  private MetricsService metricsService;

  @Mock
  private SpatialIndexIdGeneratorService spatialIndexService;

  private VehicleMapper vehicleMapper;
  private VehicleMergeMapper vehicleMergeMapper;
  private VehiclesUpdater vehiclesUpdater;

  @BeforeEach
  void setUp() {
    // Initialize real mappers
    RentalUrisMapper rentalUrisMapper = new RentalUrisMapper();
    vehicleMapper = new VehicleMapper(rentalUrisMapper);
    vehicleMergeMapper = new VehicleMergeMapperImpl();

    vehiclesUpdater =
      new VehiclesUpdater(
        vehicleCache,
        spatialIndex,
        vehicleMapper,
        vehicleMergeMapper,
        metricsService,
        spatialIndexService
      );
    // Set default values for cache TTLs
    ReflectionTestUtils.setField(vehiclesUpdater, "vehicleEntityCacheMinimumTtl", 30);
    ReflectionTestUtils.setField(vehiclesUpdater, "vehicleEntityCacheMaximumTtl", 300);
  }

  private VehicleSpatialIndexId createVehicleIndexId(String id, String systemId) {
    var indexId = new VehicleSpatialIndexId();
    indexId.setId(id);
    indexId.setSystemId(systemId);
    return indexId;
  }

  @Test
  void shouldHandleVehicleUpdate() {
    // Given
    var feedProvider = new FeedProvider();
    feedProvider.setSystemId("test-system");

    var vehicleId = "vehicle-1";
    var currentVehicle = new Vehicle();
    currentVehicle.setId(vehicleId);
    currentVehicle.setLat(59.9);
    currentVehicle.setLon(10.7);

    var updatedVehicle = new Vehicle();
    updatedVehicle.setId(vehicleId);
    updatedVehicle.setLat(59.91);
    updatedVehicle.setLon(10.71);

    var gbfsVehicle = new GBFSVehicle();
    gbfsVehicle.setVehicleId(vehicleId);
    gbfsVehicle.setLat(59.91);
    gbfsVehicle.setLon(10.71);

    var oldSpatialIndexId = createVehicleIndexId(vehicleId, "test-system");

    // Mock behavior
    when(vehicleCache.get(vehicleId)).thenReturn(currentVehicle);
    when(spatialIndexService.createVehicleIndexId(currentVehicle, feedProvider))
      .thenReturn(oldSpatialIndexId);

    var delta = new GBFSFileDelta<GBFSVehicle>(
      1000L,
      2000L,
      60L,
      "vehicle_status",
      List.of(new GBFSEntityDelta<>(vehicleId, DeltaType.UPDATE, gbfsVehicle))
    );

    // When
    vehiclesUpdater.addOrUpdateVehicles(feedProvider, delta);

    // Then
    verify(spatialIndex).removeAll(any());
    verify(spatialIndex).addAll(any());
    verify(vehicleCache).updateAll(any(), eq(300), eq(TimeUnit.SECONDS));
    verify(vehicleCache, never()).removeAll(anySet());
  }

  @Test
  void shouldHandleVehicleDelete() {
    // Given
    var feedProvider = new FeedProvider();
    feedProvider.setSystemId("test-system");

    var vehicleId = "vehicle-1";
    var currentVehicle = new Vehicle();
    currentVehicle.setId(vehicleId);
    currentVehicle.setLat(59.9);
    currentVehicle.setLon(10.7);

    var spatialIndexId = createVehicleIndexId(vehicleId, "test-system");

    // Mock behavior
    when(vehicleCache.get(vehicleId)).thenReturn(currentVehicle);
    when(spatialIndexService.createVehicleIndexId(currentVehicle, feedProvider))
      .thenReturn(spatialIndexId);

    var delta = new GBFSFileDelta<GBFSVehicle>(
      1000L,
      2000L,
      60L,
      "vehicle_status",
      List.of(new GBFSEntityDelta<>(vehicleId, DeltaType.DELETE, null))
    );

    // When
    vehiclesUpdater.addOrUpdateVehicles(feedProvider, delta);

    // Then
    verify(spatialIndex).removeAll(any());
    verify(vehicleCache).removeAll(anySet());
    verify(vehicleCache, never()).updateAll(anyMap(), anyInt(), any(TimeUnit.class));
  }

  @Test
  void shouldHandleVehicleCreate() {
    // Given
    var feedProvider = new FeedProvider();
    feedProvider.setSystemId("test-system");

    var vehicleId = "vehicle-1";
    var newVehicle = new Vehicle();
    newVehicle.setId(vehicleId);
    newVehicle.setLat(59.9);
    newVehicle.setLon(10.7);

    var gbfsVehicle = new GBFSVehicle();
    gbfsVehicle.setVehicleId(vehicleId);
    gbfsVehicle.setLat(59.9);
    gbfsVehicle.setLon(10.7);

    var spatialIndexId = createVehicleIndexId(vehicleId, "test-system");

    // Mock behavior
    when(spatialIndexService.createVehicleIndexId(any(), eq(feedProvider)))
      .thenReturn(spatialIndexId);

    var delta = new GBFSFileDelta<GBFSVehicle>(
      1000L,
      2000L,
      60L,
      "vehicle_status",
      List.of(new GBFSEntityDelta<>(vehicleId, DeltaType.CREATE, gbfsVehicle))
    );

    // When
    vehiclesUpdater.addOrUpdateVehicles(feedProvider, delta);

    // Then
    verify(spatialIndex).addAll(any());
    verify(vehicleCache).updateAll(any(), eq(300), eq(TimeUnit.SECONDS));
    verify(vehicleCache, never()).removeAll(anySet());
    verify(spatialIndex, never()).removeAll(anySet());
  }

  @Test
  void shouldHandleNonExistentVehicleUpdate() {
    // Given
    var feedProvider = new FeedProvider();
    feedProvider.setSystemId("test-system");

    var vehicleId = "vehicle-1";
    var gbfsVehicle = new GBFSVehicle();
    gbfsVehicle.setVehicleId(vehicleId);
    gbfsVehicle.setLat(59.9);
    gbfsVehicle.setLon(10.7);

    // Mock behavior
    when(vehicleCache.get(vehicleId)).thenReturn(null);

    var delta = new GBFSFileDelta<GBFSVehicle>(
      1000L,
      2000L,
      60L,
      "vehicle_status",
      List.of(new GBFSEntityDelta<>(vehicleId, DeltaType.UPDATE, gbfsVehicle))
    );

    // When
    vehiclesUpdater.addOrUpdateVehicles(feedProvider, delta);

    // Then
    verify(spatialIndex, never()).addAll(anyMap());
    verify(vehicleCache, never()).updateAll(anyMap(), anyInt(), any(TimeUnit.class));
  }
}
