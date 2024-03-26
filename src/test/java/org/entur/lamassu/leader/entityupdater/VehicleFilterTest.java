package org.entur.lamassu.leader.entityupdater;

import java.util.HashMap;
import org.entur.gbfs.v2_3.free_bike_status.GBFSBike;
import org.entur.gbfs.v2_3.vehicle_types.GBFSVehicleType;
import org.entur.gbfs.v2_3.vehicle_types.GBFSVehicleTypes;
import org.junit.Test;

public class VehicleFilterTest {

  @Test
  public void testVehicleWithoutPricingPlanIsntSkipped() {
    GBFSVehicleType vehicleType = new GBFSVehicleType();
    vehicleType.setVehicleTypeId("vehicleTypeId");
    HashMap vehicleTypes = new HashMap<String, GBFSVehicleTypes>();
    vehicleTypes.put(vehicleType.getVehicleTypeId(), vehicleType);

    VehicleFilter filter = new VehicleFilter(new HashMap<>(), vehicleTypes);

    GBFSBike vehicle = new GBFSBike();
    vehicle.setBikeId("VehicleWithoutPricingPlan");
    vehicle.setVehicleTypeId("vehicleTypeId");

    assert filter.test(vehicle) == true;
  }
}
