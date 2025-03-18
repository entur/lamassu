package org.entur.lamassu.leader.entityupdater;

import java.util.HashMap;
import java.util.Map;
import org.entur.lamassu.model.entities.PricingPlan;
import org.entur.lamassu.model.entities.Station;
import org.entur.lamassu.model.entities.VehicleType;
import org.entur.lamassu.stubs.EntityCacheStub;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mobilitydata.gbfs.v3_0.vehicle_status.GBFSVehicle;
import org.springframework.test.util.ReflectionTestUtils;

class VehicleFilterTest {

  private static final String VEHICLE_TYPE_ID = "vehicleTypeId";

  private VehicleFilter filter;
  private EntityCacheStub<PricingPlan> pricingPlanCache;
  private EntityCacheStub<VehicleType> vehicleTypeCache;
  private EntityCacheStub<Station> stationCache;

  @BeforeEach
  public void setUp() {
    pricingPlanCache = new EntityCacheStub<>();
    vehicleTypeCache = new EntityCacheStub<>();
    stationCache = new EntityCacheStub<>();
    filter = new VehicleFilter(pricingPlanCache, vehicleTypeCache, stationCache);
    ReflectionTestUtils.setField(
      filter,
      "includeVehiclesAssignedToNonVirtualStations",
      false
    );
  }

  private HashMap<String, VehicleType> vehicleTypesWithPricingPlan(
    String optionalPricingPlanId
  ) {
    VehicleType vehicleType = new VehicleType();
    vehicleType.setDefaultPricingPlanId(optionalPricingPlanId);
    HashMap<String, VehicleType> vehicleTypes = new HashMap<>();
    vehicleTypes.put(VEHICLE_TYPE_ID, vehicleType);

    return vehicleTypes;
  }

  @Test
  void testVehicleWithoutPricingPlanButDefaultPricingPlanIsntSkipped() {
    vehicleTypeCache.updateAll(vehicleTypesWithPricingPlan("pricingPlanId"), 0, null);

    GBFSVehicle vehicle = new GBFSVehicle();
    vehicle.setVehicleId("VehicleWithoutPricingPlan");
    vehicle.setVehicleTypeId(VEHICLE_TYPE_ID);

    assert filter.test(vehicle);
  }

  @Test
  void testVehicleWithoutPricingPlanAndWithoutDefaultPricingPlanIsSkipped() {
    vehicleTypeCache.updateAll(vehicleTypesWithPricingPlan(null), 0, null);

    GBFSVehicle vehicle = new GBFSVehicle();
    vehicle.setVehicleId("VehicleWithoutPricingPlan");
    vehicle.setVehicleTypeId("vehicleTypeId");

    assert !filter.test(vehicle);
  }

  @Test
  void testVehicleWithUnknownPricingPlanIsSkipped() {
    vehicleTypeCache.updateAll(vehicleTypesWithPricingPlan(null), 0, null);

    GBFSVehicle vehicle = new GBFSVehicle();
    vehicle.setVehicleId("VehicleWithUnknownPricingPlan");
    vehicle.setVehicleTypeId(VEHICLE_TYPE_ID);
    vehicle.setPricingPlanId("unknownPricingPlanId");

    assert !filter.test(vehicle);
  }

  @Test
  void testVehicleWithUnknownVehicleTypeIsSkipped() {
    GBFSVehicle vehicle = new GBFSVehicle();
    vehicle.setVehicleId("VehicleWithUnknownType");
    vehicle.setVehicleTypeId("unknownVehicleTypeId");

    assert !filter.test(vehicle);
  }

  @Test
  void testVehicleWithValidPricingPlanIsNotSkipped() {
    String validPricingPlanId = "validPricingPlanId";
    PricingPlan pricingPlan = new PricingPlan();
    pricingPlan.setId(validPricingPlanId);
    Map<String, PricingPlan> pricingPlans = new HashMap<>();
    pricingPlans.put(validPricingPlanId, pricingPlan);
    pricingPlanCache.updateAll(pricingPlans, 0, null);
    vehicleTypeCache.updateAll(vehicleTypesWithPricingPlan(null), 0, null);

    GBFSVehicle vehicle = new GBFSVehicle();
    vehicle.setVehicleId("VehicleWithValidPricingPlan");
    vehicle.setVehicleTypeId(VEHICLE_TYPE_ID);
    vehicle.setPricingPlanId(validPricingPlanId);

    assert filter.test(vehicle);
  }

  @Test
  void testVehicleAtNonVirtualStationIsSkippedByDefault() {
    vehicleTypeCache.updateAll(vehicleTypesWithPricingPlan(null), 0, null);

    String stationId = "nonVirtualStation";
    Station station = new Station();
    station.setId(stationId);
    station.setVirtualStation(false);
    Map<String, Station> stations = new HashMap<>();
    stations.put(stationId, station);
    stationCache.updateAll(stations, 0, null);

    GBFSVehicle vehicle = new GBFSVehicle();
    vehicle.setVehicleId("VehicleAtNonVirtualStation");
    vehicle.setVehicleTypeId(VEHICLE_TYPE_ID);
    vehicle.setStationId(stationId);

    assert !filter.test(vehicle);
  }

  @Test
  void testVehicleAtVirtualStationIsNotSkipped() {
    String defaultPricingPlanId = "defaultPricingPlanId";
    PricingPlan pricingPlan = new PricingPlan();
    pricingPlan.setId(defaultPricingPlanId);
    Map<String, PricingPlan> pricingPlans = new HashMap<>();
    pricingPlans.put(defaultPricingPlanId, pricingPlan);
    pricingPlanCache.updateAll(pricingPlans, 0, null);
    vehicleTypeCache.updateAll(
      vehicleTypesWithPricingPlan(defaultPricingPlanId),
      0,
      null
    );

    String stationId = "virtualStation";
    Station station = new Station();
    station.setId(stationId);
    station.setVirtualStation(true);
    Map<String, Station> stations = new HashMap<>();
    stations.put(stationId, station);
    stationCache.updateAll(stations, 0, null);

    GBFSVehicle vehicle = new GBFSVehicle();
    vehicle.setVehicleId("VehicleAtVirtualStation");
    vehicle.setVehicleTypeId(VEHICLE_TYPE_ID);
    vehicle.setStationId(stationId);

    assert filter.test(vehicle);
  }

  @Test
  void testVehicleAtStationWithoutIsVirtualSetIsSkipped() {
    String defaultPricingPlanId = "defaultPricingPlanId";
    PricingPlan pricingPlan = new PricingPlan();
    pricingPlan.setId(defaultPricingPlanId);
    Map<String, PricingPlan> pricingPlans = new HashMap<>();
    pricingPlans.put(defaultPricingPlanId, pricingPlan);
    pricingPlanCache.updateAll(pricingPlans, 0, null);
    vehicleTypeCache.updateAll(
      vehicleTypesWithPricingPlan(defaultPricingPlanId),
      0,
      null
    );

    String stationId = "virtualStation";
    Station station = new Station();
    station.setId(stationId);
    station.setVirtualStation(null);
    Map<String, Station> stations = new HashMap<>();
    stations.put(stationId, station);
    stationCache.updateAll(stations, 0, null);

    GBFSVehicle vehicle = new GBFSVehicle();
    vehicle.setVehicleId("VehicleAtVirtualStation");
    vehicle.setVehicleTypeId(VEHICLE_TYPE_ID);
    vehicle.setStationId(stationId);

    assert !filter.test(vehicle);
  }
}
