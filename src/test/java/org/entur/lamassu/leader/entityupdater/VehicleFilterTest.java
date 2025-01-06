package org.entur.lamassu.leader.entityupdater;

import org.junit.Test;
import org.mobilitydata.gbfs.v3_0.vehicle_status.GBFSVehicle;

public class VehicleFilterTest {

  @Test
  public void testHybridVehicleIsSkipped() {
    VehicleFilter filter = new VehicleFilter();
    GBFSVehicle vehicle = new GBFSVehicle();
    vehicle.setStationId("TST:Station:abc123");
    assert filter.test(vehicle) == false;
  }

  @Test
  public void testNonHybridVehicleIsNotSkipped() {
    VehicleFilter filter = new VehicleFilter();
    GBFSVehicle vehicle = new GBFSVehicle();
    assert filter.test(vehicle) == true;
  }
}
