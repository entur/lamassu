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
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.entur.lamassu.cache.EntityCache;
import org.entur.lamassu.cache.VehicleSpatialIndex;
import org.entur.lamassu.delta.DeltaType;
import org.entur.lamassu.delta.GBFSEntityDelta;
import org.entur.lamassu.delta.GBFSFileDelta;
import org.entur.lamassu.mapper.entitymapper.RentalUrisMapper;
import org.entur.lamassu.mapper.entitymapper.VehicleMapper;
import org.entur.lamassu.mapper.entitymapper.VehicleMergeMapper;
import org.entur.lamassu.mapper.entitymapper.VehicleMergeMapperImpl;
import org.entur.lamassu.metrics.MetricsService;
import org.entur.lamassu.model.entities.FormFactor;
import org.entur.lamassu.model.entities.PropulsionType;
import org.entur.lamassu.model.entities.Vehicle;
import org.entur.lamassu.model.entities.VehicleType;
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
  private EntityCache<VehicleType> vehicleTypeCache;

  private VehicleMapper vehicleMapper;
  private VehicleMergeMapper vehicleMergeMapper;
  private SpatialIndexIdGeneratorService spatialIndexIdGeneratorService;
  private VehiclesUpdater vehiclesUpdater;

  @BeforeEach
  void setUp() {
    // Initialize real mappers
    RentalUrisMapper rentalUrisMapper = new RentalUrisMapper();
    vehicleMapper = new VehicleMapper(rentalUrisMapper);
    vehicleMergeMapper = new VehicleMergeMapperImpl();

    // Initialize real services
    spatialIndexIdGeneratorService = new SpatialIndexIdGeneratorService(vehicleTypeCache);

    vehiclesUpdater =
      new VehiclesUpdater(
        vehicleCache,
        spatialIndex,
        vehicleMapper,
        vehicleMergeMapper,
        metricsService,
        spatialIndexIdGeneratorService
      );
  }

  @Test
  void shouldHandleVehicleUpdate() {
    // Given
    var feedProvider = new FeedProvider();
    feedProvider.setSystemId("test-system");
    feedProvider.setCodespace("test");
    feedProvider.setOperatorId("test-operator");
    feedProvider.setLanguage("en");

    var vehicleId = "vehicle-1";
    var bikeTypeId = "bike";

    var bikeType = new VehicleType();
    bikeType.setId(bikeTypeId);
    bikeType.setFormFactor(FormFactor.BICYCLE);
    bikeType.setPropulsionType(PropulsionType.HUMAN);

    var currentVehicle = new Vehicle();
    currentVehicle.setId(vehicleId);
    currentVehicle.setLat(59.9);
    currentVehicle.setLon(10.7);
    currentVehicle.setVehicleTypeId(bikeTypeId);
    currentVehicle.setReserved(false);
    currentVehicle.setDisabled(false);

    var gbfsVehicle = new GBFSVehicle();
    gbfsVehicle.setVehicleId(vehicleId);
    gbfsVehicle.setLat(59.91);
    gbfsVehicle.setLon(10.71);

    // Mock behavior
    when(vehicleCache.get(vehicleId)).thenReturn(currentVehicle);
    when(vehicleTypeCache.get(bikeTypeId)).thenReturn(bikeType);

    var delta = new GBFSFileDelta<GBFSVehicle>(
      30000L,
      60000L,
      300L,
      "vehicle_status",
      List.of(new GBFSEntityDelta<>(vehicleId, DeltaType.UPDATE, gbfsVehicle))
    );

    // Calculate expected spatial index ID for verification
    var oldSpatialIndexId = spatialIndexIdGeneratorService.createVehicleIndexId(
      currentVehicle,
      feedProvider
    );

    // When
    vehiclesUpdater.addOrUpdateVehicles(feedProvider, delta);

    // Then
    verify(spatialIndex).removeAll(Set.of(oldSpatialIndexId));
    verify(spatialIndex).addAll(any());
    verify(vehicleCache).updateAll(any());
    verify(vehicleCache, never()).removeAll(anySet());
  }

  @Test
  void shouldHandleVehicleDelete() {
    // Given
    var feedProvider = new FeedProvider();
    feedProvider.setSystemId("test-system");
    feedProvider.setCodespace("test");
    feedProvider.setOperatorId("test-operator");
    feedProvider.setLanguage("en");

    var vehicleId = "vehicle-1";
    var bikeTypeId = "bike";

    var bikeType = new VehicleType();
    bikeType.setId(bikeTypeId);
    bikeType.setFormFactor(FormFactor.BICYCLE);
    bikeType.setPropulsionType(PropulsionType.HUMAN);

    var currentVehicle = new Vehicle();
    currentVehicle.setId(vehicleId);
    currentVehicle.setLat(59.9);
    currentVehicle.setLon(10.7);
    currentVehicle.setVehicleTypeId(bikeTypeId);
    currentVehicle.setReserved(false);
    currentVehicle.setDisabled(false);

    // Mock behavior
    when(vehicleCache.get(vehicleId)).thenReturn(currentVehicle);
    when(vehicleTypeCache.get(bikeTypeId)).thenReturn(bikeType);

    var delta = new GBFSFileDelta<GBFSVehicle>(
      1000L,
      2000L,
      60L,
      "vehicle_status",
      List.of(new GBFSEntityDelta<>(vehicleId, DeltaType.DELETE, null))
    );

    // Calculate expected spatial index ID for verification
    var spatialIndexId = spatialIndexIdGeneratorService.createVehicleIndexId(
      currentVehicle,
      feedProvider
    );

    // When
    vehiclesUpdater.addOrUpdateVehicles(feedProvider, delta);

    // Then
    verify(spatialIndex).removeAll(Set.of(spatialIndexId));
    verify(vehicleCache).removeAll(Set.of(vehicleId));
    verify(vehicleCache, never()).updateAll(anyMap(), anyInt(), any(TimeUnit.class));
  }

  @Test
  void shouldHandleVehicleCreate() {
    // Given
    var feedProvider = new FeedProvider();
    feedProvider.setSystemId("test-system");
    feedProvider.setCodespace("test");
    feedProvider.setOperatorId("test-operator");
    feedProvider.setLanguage("en");

    var vehicleId = "vehicle-1";
    var bikeTypeId = "bike";

    var bikeType = new VehicleType();
    bikeType.setId(bikeTypeId);
    bikeType.setFormFactor(FormFactor.BICYCLE);
    bikeType.setPropulsionType(PropulsionType.HUMAN);

    var gbfsVehicle = new GBFSVehicle();
    gbfsVehicle.setVehicleId(vehicleId);
    gbfsVehicle.setLat(59.9);
    gbfsVehicle.setLon(10.7);
    gbfsVehicle.setVehicleTypeId(bikeTypeId);
    gbfsVehicle.setIsReserved(false);
    gbfsVehicle.setIsDisabled(false);

    // Mock behavior
    when(vehicleTypeCache.get(bikeTypeId)).thenReturn(bikeType);

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
    verify(vehicleCache).updateAll(any());
    verify(vehicleCache, never()).removeAll(anySet());
    verify(spatialIndex, never()).removeAll(anySet());
  }

  @Test
  void shouldHandleNonExistentVehicleUpdate() {
    // Given
    var feedProvider = new FeedProvider();
    feedProvider.setSystemId("test-system");
    feedProvider.setCodespace("test");
    feedProvider.setOperatorId("test-operator");
    feedProvider.setLanguage("en");

    var vehicleId = "vehicle-1";
    var bikeTypeId = "bike";

    var gbfsVehicle = new GBFSVehicle();
    gbfsVehicle.setVehicleId(vehicleId);
    gbfsVehicle.setLat(59.9);
    gbfsVehicle.setLon(10.7);
    gbfsVehicle.setVehicleTypeId(bikeTypeId);
    gbfsVehicle.setIsReserved(false);
    gbfsVehicle.setIsDisabled(false);

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
