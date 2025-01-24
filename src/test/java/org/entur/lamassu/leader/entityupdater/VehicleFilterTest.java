package org.entur.lamassu.leader.entityupdater;

import java.util.HashMap;
import org.entur.lamassu.model.entities.PricingPlan;
import org.entur.lamassu.model.entities.Station;
import org.entur.lamassu.model.entities.VehicleType;
import org.entur.lamassu.stubs.EntityCacheStub;
import org.junit.Test;
import org.mobilitydata.gbfs.v3_0.vehicle_status.GBFSVehicle;

public class VehicleFilterTest {

  private static String VEHICLE_TYPE_ID = "vehicleTypeId";

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
  public void testVehicleWithoutPricingPlanButDefaultPricingPlanIsntSkipped() {
    var pricingPlanCache = new EntityCacheStub<PricingPlan>();
    var vehicleTypeCache = new EntityCacheStub<VehicleType>();
    var stationCache = new EntityCacheStub<Station>();

    vehicleTypeCache.updateAll(vehicleTypesWithPricingPlan("pricingPlanId"), 0, null);

    VehicleFilter filter = new VehicleFilter(
      pricingPlanCache,
      vehicleTypeCache,
      stationCache
    );

    GBFSVehicle vehicle = new GBFSVehicle();
    vehicle.setVehicleId("VehicleWithoutPricingPlan");
    vehicle.setVehicleTypeId(VEHICLE_TYPE_ID);

    assert filter.test(vehicle) == true;
  }

  @Test
  public void testVehicleWithoutPricingPlanAndWithoutDefaultPricingPlanIsSkipped() {
    var pricingPlanCache = new EntityCacheStub<PricingPlan>();
    var vehicleTypeCache = new EntityCacheStub<VehicleType>();
    var stationCache = new EntityCacheStub<Station>();

    vehicleTypeCache.updateAll(vehicleTypesWithPricingPlan(null), 0, null);

    VehicleFilter filter = new VehicleFilter(
      pricingPlanCache,
      vehicleTypeCache,
      stationCache
    );

    GBFSVehicle vehicle = new GBFSVehicle();
    vehicle.setVehicleId("VehicleWithoutPricingPlan");
    vehicle.setVehicleTypeId("vehicleTypeId");

    assert filter.test(vehicle) == false;
  }
}
