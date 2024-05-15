package org.entur.lamassu.leader.entityupdater;

import java.util.HashMap;
import org.entur.lamassu.model.entities.PricingPlan;
import org.entur.lamassu.model.entities.VehicleType;
import org.junit.Test;
import org.mobilitydata.gbfs.v2_3.free_bike_status.GBFSBike;

public class VehicleFilterTest {

  private static String VEHICLE_TYPE_ID = "vehicleTypeId";

  private HashMap<String, VehicleType> vehicleTypesWithPricingPlan(
    PricingPlan optionalPricingPlan
  ) {
    VehicleType vehicleType = new VehicleType();
    vehicleType.setDefaultPricingPlan(optionalPricingPlan);
    HashMap<String, VehicleType> vehicleTypes = new HashMap<>();
    vehicleTypes.put(VEHICLE_TYPE_ID, vehicleType);

    return vehicleTypes;
  }

  @Test
  public void testVehicleWithoutPricingPlanButDefaultPricingPlanIsntSkipped() {
    VehicleFilter filter = new VehicleFilter(
      new HashMap<>(),
      vehicleTypesWithPricingPlan(new PricingPlan())
    );

    GBFSBike vehicle = new GBFSBike();
    vehicle.setBikeId("VehicleWithoutPricingPlan");
    vehicle.setVehicleTypeId("vehicleTypeId");

    assert filter.test(vehicle) == true;
  }

  @Test
  public void testVehicleWithoutPricingPlanAndWithoutDefaultPricingPlanIsSkipped() {
    VehicleFilter filter = new VehicleFilter(
      new HashMap<>(),
      vehicleTypesWithPricingPlan(null)
    );

    GBFSBike vehicle = new GBFSBike();
    vehicle.setBikeId("VehicleWithoutPricingPlan");
    vehicle.setVehicleTypeId("vehicleTypeId");

    assert filter.test(vehicle) == false;
  }
}
