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
      SpatialIndexIdFilter.filterVehicle(aVehicleId(), aVehicleFilterParams())
    );
  }

  @Test
  public void testNoFilterReturnsStationWithoutVehicleTypesAvailable() {
    StationSpatialIndexId stationSpatialIndexId =
      TestSpatialIndexBuilder.createStationIndexId(
        aStationWithoutVehicleTypeAvailability(),
        aProvider()
      );
    Assert.assertTrue(
      SpatialIndexIdFilter.filterStation(stationSpatialIndexId, aStationFilterParams())
    );
  }

  @Test
  public void testCodespaceFilter() {
    var testId = aVehicleId();
    var params = aVehicleFilterParams();

    params.setCodespaces(List.of("TST"));
    Assert.assertTrue(SpatialIndexIdFilter.filterVehicle(testId, params));

    params.setCodespaces(List.of("FOO"));
    Assert.assertFalse(SpatialIndexIdFilter.filterVehicle(testId, params));
  }

  @Test
  public void testSystemFilter() {
    var testId = aVehicleId();
    var params = aVehicleFilterParams();

    params.setSystems(List.of("TST:System:testprovider"));
    Assert.assertTrue(SpatialIndexIdFilter.filterVehicle(testId, params));

    params.setSystems(List.of("FOO:System:foo"));
    Assert.assertFalse(SpatialIndexIdFilter.filterVehicle(testId, params));
  }

  @Test
  public void testOperatorFilter() {
    var testId = aVehicleId();
    var params = aVehicleFilterParams();

    params.setOperators(List.of("TST:Operator:test"));
    Assert.assertTrue(SpatialIndexIdFilter.filterVehicle(testId, params));

    params.setOperators(List.of("FOO:Operator:foo"));
    Assert.assertFalse(SpatialIndexIdFilter.filterVehicle(testId, params));
  }

  @Test
  public void testFormFactorFilter() {
    var testId = aVehicleId();
    var params = aVehicleFilterParams();

    params.setFormFactors(List.of(FormFactor.SCOOTER));
    Assert.assertTrue(SpatialIndexIdFilter.filterVehicle(testId, params));

    params.setFormFactors(List.of(FormFactor.BICYCLE));
    Assert.assertFalse(SpatialIndexIdFilter.filterVehicle(testId, params));
  }

  @Test
  public void testPropulsionTypeFilter() {
    var testId = aVehicleId();
    var params = aVehicleFilterParams();

    params.setPropulsionTypes(List.of(PropulsionType.ELECTRIC));
    Assert.assertTrue(SpatialIndexIdFilter.filterVehicle(testId, params));

    params.setPropulsionTypes(List.of(PropulsionType.COMBUSTION));
    Assert.assertFalse(SpatialIndexIdFilter.filterVehicle(testId, params));
  }

  @Test
  public void testIncludeReservedFilter() {
    var testId = aReservedId();
    var params = aVehicleFilterParams();

    params.setIncludeReserved(true);
    Assert.assertTrue(SpatialIndexIdFilter.filterVehicle(testId, params));

    params.setIncludeReserved(false);
    Assert.assertFalse(SpatialIndexIdFilter.filterVehicle(testId, params));
  }

  @Test
  public void testIncludeDisabledFilter() {
    var testId = aDisabledId();
    var params = aVehicleFilterParams();

    params.setIncludeDisabled(true);
    Assert.assertTrue(SpatialIndexIdFilter.filterVehicle(testId, params));

    params.setIncludeDisabled(false);
    Assert.assertFalse(SpatialIndexIdFilter.filterVehicle(testId, params));
  }

  @Test
  public void testVehicleTypesAvailableFilter() {
    var testId = aStationId();
    var params = new StationFilterParameters(
      null,
      null,
      null,
      null,
      List.of(FormFactor.SCOOTER),
      List.of(PropulsionType.ELECTRIC)
    );

    Assert.assertTrue(SpatialIndexIdFilter.filterStation(testId, params));

    params.setAvailableFormFactors(List.of(FormFactor.BICYCLE));

    Assert.assertFalse(SpatialIndexIdFilter.filterStation(testId, params));

    params.setAvailableFormFactors(null);

    params.setAvailablePropulsionTypes(List.of(PropulsionType.HUMAN));

    Assert.assertFalse(SpatialIndexIdFilter.filterStation(testId, params));
  }

  private VehicleSpatialIndexId aVehicleId() {
    return TestSpatialIndexBuilder.createVehicleIndexId(aVehicle(), aProvider());
  }

  private StationSpatialIndexId aStationId() {
    return TestSpatialIndexBuilder.createStationIndexId(aStation(), aProvider());
  }

  private VehicleSpatialIndexId aReservedId() {
    var vehicle = aVehicle();
    vehicle.setReserved(true);
    return TestSpatialIndexBuilder.createVehicleIndexId(vehicle, aProvider());
  }

  private VehicleSpatialIndexId aDisabledId() {
    var vehicle = aVehicle();
    vehicle.setDisabled(true);
    return TestSpatialIndexBuilder.createVehicleIndexId(vehicle, aProvider());
  }

  private Vehicle aVehicle() {
    var vehicle = new Vehicle();
    vehicle.setId("TST:Vehicle:abc123");
    vehicle.setReserved(false);
    vehicle.setDisabled(false);
    vehicle.setVehicleType(aScooterVehicle());
    return vehicle;
  }

  private Station aStation() {
    var station = new Station();
    station.setId("TST:Station:foobar");
    var vehicleTypeAvailability = new VehicleTypeAvailability();
    vehicleTypeAvailability.setVehicleType(aScooterVehicle());
    vehicleTypeAvailability.setCount(1);
    station.setVehicleTypesAvailable(List.of(vehicleTypeAvailability));
    return station;
  }

  private Station aStationWithoutVehicleTypeAvailability() {
    var station = new Station();
    station.setId("TST:Station:no_vta");
    return station;
  }

  private VehicleType aScooterVehicle() {
    var type = new VehicleType();
    type.setId("TST:VehicleType:Scooter");
    type.setFormFactor(FormFactor.SCOOTER);
    type.setPropulsionType(PropulsionType.ELECTRIC);
    return type;
  }

  private FeedProvider aProvider() {
    var provider = new FeedProvider();
    provider.setCodespace("TST");
    provider.setSystemId("TST:System:testprovider");
    provider.setOperatorId("TST:Operator:test");
    provider.setOperatorName("testprovider");
    return provider;
  }

  private VehicleFilterParameters aVehicleFilterParams() {
    return new VehicleFilterParameters(null, null, null, null, null, null, false, false);
  }

  private StationFilterParameters aStationFilterParams() {
    return new StationFilterParameters(null, null, null, null, null, null);
  }
}
