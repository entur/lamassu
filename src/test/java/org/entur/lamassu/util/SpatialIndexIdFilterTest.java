package org.entur.lamassu.util;

import java.util.List;
import org.entur.lamassu.cache.StationSpatialIndexId;
import org.entur.lamassu.cache.VehicleSpatialIndexId;
import org.entur.lamassu.model.entities.FormFactor;
import org.entur.lamassu.model.entities.PropulsionType;
import org.entur.lamassu.model.entities.Station;
import org.entur.lamassu.model.entities.Vehicle;
import org.entur.lamassu.model.entities.VehicleType;
import org.entur.lamassu.model.entities.VehicleTypeAvailability;
import org.entur.lamassu.model.provider.FeedProvider;
import org.entur.lamassu.service.StationFilterParameters;
import org.entur.lamassu.service.VehicleFilterParameters;
import org.junit.Assert;
import org.junit.Test;

public class SpatialIndexIdFilterTest {

  @Test
  public void testNoFilter() {
    Assert.assertTrue(
      SpatialIndexIdFilter.filterVehicle(testVehicleId(), testVehicleFilterParams())
    );
  }

  @Test
  public void testNoFilterReturnsStationWithoutVehicleTypesAvailable() {
    StationSpatialIndexId stationSpatialIndexId = SpatialIndexIdUtil.createStationSpatialIndexId(testStationWithoutVehicleTypeAvailability(), testProvider());
    Assert.assertTrue(
            SpatialIndexIdFilter.filterStation(stationSpatialIndexId, testStationFilterParams())
    );
  }

  @Test
  public void testCodespaceFilter() {
    var testId = testVehicleId();
    var params = testVehicleFilterParams();

    params.setCodespaces(List.of("TST"));
    Assert.assertTrue(SpatialIndexIdFilter.filterVehicle(testId, params));

    params.setCodespaces(List.of("FOO"));
    Assert.assertFalse(SpatialIndexIdFilter.filterVehicle(testId, params));
  }

  @Test
  public void testSystemFilter() {
    var testId = testVehicleId();
    var params = testVehicleFilterParams();

    params.setSystems(List.of("TST:System:testprovider"));
    Assert.assertTrue(SpatialIndexIdFilter.filterVehicle(testId, params));

    params.setSystems(List.of("FOO:System:foo"));
    Assert.assertFalse(SpatialIndexIdFilter.filterVehicle(testId, params));
  }

  @Test
  public void testOperatorFilter() {
    var testId = testVehicleId();
    var params = testVehicleFilterParams();

    params.setOperators(List.of("TST:Operator:test"));
    Assert.assertTrue(SpatialIndexIdFilter.filterVehicle(testId, params));

    params.setOperators(List.of("FOO:Operator:foo"));
    Assert.assertFalse(SpatialIndexIdFilter.filterVehicle(testId, params));
  }

  @Test
  public void testFormFactorFilter() {
    var testId = testVehicleId();
    var params = testVehicleFilterParams();

    params.setFormFactors(List.of(FormFactor.SCOOTER));
    Assert.assertTrue(SpatialIndexIdFilter.filterVehicle(testId, params));

    params.setFormFactors(List.of(FormFactor.BICYCLE));
    Assert.assertFalse(SpatialIndexIdFilter.filterVehicle(testId, params));
  }

  @Test
  public void testPropulsionTypeFilter() {
    var testId = testVehicleId();
    var params = testVehicleFilterParams();

    params.setPropulsionTypes(List.of(PropulsionType.ELECTRIC));
    Assert.assertTrue(SpatialIndexIdFilter.filterVehicle(testId, params));

    params.setPropulsionTypes(List.of(PropulsionType.COMBUSTION));
    Assert.assertFalse(SpatialIndexIdFilter.filterVehicle(testId, params));
  }

  @Test
  public void testIncludeReservedFilter() {
    var testId = testReservedId();
    var params = testVehicleFilterParams();

    params.setIncludeReserved(true);
    Assert.assertTrue(SpatialIndexIdFilter.filterVehicle(testId, params));

    params.setIncludeReserved(false);
    Assert.assertFalse(SpatialIndexIdFilter.filterVehicle(testId, params));
  }

  @Test
  public void testIncludeDisabledFilter() {
    var testId = testDisabledId();
    var params = testVehicleFilterParams();

    params.setIncludeDisabled(true);
    Assert.assertTrue(SpatialIndexIdFilter.filterVehicle(testId, params));

    params.setIncludeDisabled(false);
    Assert.assertFalse(SpatialIndexIdFilter.filterVehicle(testId, params));
  }

  @Test
  public void testVehicleTypesAvailableFilter() {
    var testId = testStationId();
    var params = new StationFilterParameters();

    params.setAvailableFormFactors(List.of(FormFactor.SCOOTER));
    params.setAvailablePropulsionTypes(List.of(PropulsionType.ELECTRIC));

    Assert.assertTrue(SpatialIndexIdFilter.filterStation(testId, params));

    params.setAvailableFormFactors(List.of(FormFactor.BICYCLE));

    Assert.assertFalse(SpatialIndexIdFilter.filterStation(testId, params));

    params.setAvailableFormFactors(null);

    params.setAvailablePropulsionTypes(List.of(PropulsionType.HUMAN));

    Assert.assertFalse(SpatialIndexIdFilter.filterStation(testId, params));
  }

  private VehicleSpatialIndexId testVehicleId() {
    return SpatialIndexIdUtil.createVehicleSpatialIndexId(testVehicle(), testProvider());
  }

  private StationSpatialIndexId testStationId() {
    return SpatialIndexIdUtil.createStationSpatialIndexId(testStation(), testProvider());
  }

  private VehicleSpatialIndexId testReservedId() {
    var vehicle = testVehicle();
    vehicle.setReserved(true);
    return SpatialIndexIdUtil.createVehicleSpatialIndexId(vehicle, testProvider());
  }

  private VehicleSpatialIndexId testDisabledId() {
    var vehicle = testVehicle();
    vehicle.setDisabled(true);
    return SpatialIndexIdUtil.createVehicleSpatialIndexId(vehicle, testProvider());
  }

  private Vehicle testVehicle() {
    var vehicle = new Vehicle();
    vehicle.setId("TST:Vehicle:abc123");
    vehicle.setReserved(false);
    vehicle.setDisabled(false);
    vehicle.setVehicleType(scooterVehicle());
    return vehicle;
  }

  private Station testStation() {
    var station = new Station();
    station.setId("TST:Station:foobar");
    var vehicleTypeAvailability = new VehicleTypeAvailability();
    vehicleTypeAvailability.setVehicleType(scooterVehicle());
    vehicleTypeAvailability.setCount(1);
    station.setVehicleTypesAvailable(List.of(vehicleTypeAvailability));
    return station;
  }

  private Station testStationWithoutVehicleTypeAvailability() {
    var station = new Station();
    station.setId("TST:Station:no_vta");
    return station;
  }

  private VehicleType scooterVehicle() {
    var type = new VehicleType();
    type.setId("TST:VehicleType:Scooter");
    type.setFormFactor(FormFactor.SCOOTER);
    type.setPropulsionType(PropulsionType.ELECTRIC);
    return type;
  }

  private FeedProvider testProvider() {
    var provider = new FeedProvider();
    provider.setCodespace("TST");
    provider.setSystemId("TST:System:testprovider");
    provider.setOperatorId("TST:Operator:test");
    provider.setOperatorName("testprovider");
    return provider;
  }

  private VehicleFilterParameters testVehicleFilterParams() {
    var params = new VehicleFilterParameters();
    params.setIncludeReserved(false);
    params.setIncludeDisabled(false);
    return params;
  }

  private StationFilterParameters testStationFilterParams() {
    var params = new StationFilterParameters();
    return params;
  }
}
