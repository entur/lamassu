package org.entur.lamassu.leader.entityupdater;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;
import org.entur.gbfs.loader.v3.GbfsV3Delivery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mobilitydata.gbfs.v3_0.station_status.GBFSStationStatus;
import org.mobilitydata.gbfs.v3_0.vehicle_status.GBFSVehicleStatus;

class GbfsUpdateContinuityTrackerTest {

  private static final String SYSTEM_ID = "test-system";
  private GbfsUpdateContinuityTracker tracker;

  @BeforeEach
  void setUp() {
    tracker = new GbfsUpdateContinuityTracker();
  }

  @Test
  void hasVehicleUpdateContinuity_shouldReturnFalse_whenOldDeliveryIsNull() {
    var nextDelivery = createDeliveryWithVehicleStatus(1000L);
    var oldDelivery = createDeliveryWithVehicleStatus(null);

    assertFalse(tracker.hasVehicleUpdateContinuity(SYSTEM_ID, oldDelivery, nextDelivery));
  }

  @Test
  void hasVehicleUpdateContinuity_shouldReturnFalse_whenFirstUpdate() {
    var nextDelivery = createDeliveryWithVehicleStatus(1000L);
    var oldDelivery = createDeliveryWithVehicleStatus(500L);

    assertFalse(tracker.hasVehicleUpdateContinuity(SYSTEM_ID, oldDelivery, nextDelivery));
  }

  @Test
  void hasVehicleUpdateContinuity_shouldReturnTrue_whenTimestampsMatch() {
    var nextDelivery = createDeliveryWithVehicleStatus(1000L);
    var oldDelivery = createDeliveryWithVehicleStatus(500L);

    // First update establishes the base
    tracker.hasVehicleUpdateContinuity(SYSTEM_ID, oldDelivery, nextDelivery);

    // Second update with matching timestamps
    var nextDelivery2 = createDeliveryWithVehicleStatus(1500L);
    assertTrue(
      tracker.hasVehicleUpdateContinuity(SYSTEM_ID, nextDelivery, nextDelivery2)
    );
  }

  @Test
  void hasVehicleUpdateContinuity_shouldReturnFalse_whenTimestampsDontMatch() {
    var nextDelivery = createDeliveryWithVehicleStatus(1000L);
    var oldDelivery = createDeliveryWithVehicleStatus(500L);

    // First update establishes the base
    tracker.hasVehicleUpdateContinuity(SYSTEM_ID, oldDelivery, nextDelivery);

    // Second update with non-matching timestamps (simulating missed update)
    var nextDelivery2 = createDeliveryWithVehicleStatus(1500L);
    var modifiedOldDelivery = createDeliveryWithVehicleStatus(800L); // Different from stored base
    assertFalse(
      tracker.hasVehicleUpdateContinuity(SYSTEM_ID, modifiedOldDelivery, nextDelivery2)
    );
  }

  @Test
  void hasStationUpdateContinuity_shouldReturnFalse_whenOldDeliveryIsNull() {
    var nextDelivery = createDeliveryWithStationStatus(1000L);
    var oldDelivery = createDeliveryWithStationStatus(null);

    assertFalse(tracker.hasStationUpdateContinuity(SYSTEM_ID, oldDelivery, nextDelivery));
  }

  @Test
  void hasStationUpdateContinuity_shouldReturnFalse_whenFirstUpdate() {
    var nextDelivery = createDeliveryWithStationStatus(1000L);
    var oldDelivery = createDeliveryWithStationStatus(500L);

    assertFalse(tracker.hasStationUpdateContinuity(SYSTEM_ID, oldDelivery, nextDelivery));
  }

  @Test
  void hasStationUpdateContinuity_shouldReturnTrue_whenTimestampsMatch() {
    var nextDelivery = createDeliveryWithStationStatus(1000L);
    var oldDelivery = createDeliveryWithStationStatus(500L);

    // First update establishes the base
    tracker.hasStationUpdateContinuity(SYSTEM_ID, oldDelivery, nextDelivery);

    // Second update with matching timestamps
    var nextDelivery2 = createDeliveryWithStationStatus(1500L);
    assertTrue(
      tracker.hasStationUpdateContinuity(SYSTEM_ID, nextDelivery, nextDelivery2)
    );
  }

  @Test
  void hasStationUpdateContinuity_shouldReturnFalse_whenTimestampsDontMatch() {
    var nextDelivery = createDeliveryWithStationStatus(1000L);
    var oldDelivery = createDeliveryWithStationStatus(500L);

    // First update establishes the base
    tracker.hasStationUpdateContinuity(SYSTEM_ID, oldDelivery, nextDelivery);

    // Second update with non-matching timestamps (simulating missed update)
    var nextDelivery2 = createDeliveryWithStationStatus(1500L);
    var modifiedOldDelivery = createDeliveryWithStationStatus(800L); // Different from stored base
    assertFalse(
      tracker.hasStationUpdateContinuity(SYSTEM_ID, modifiedOldDelivery, nextDelivery2)
    );
  }

  private GbfsV3Delivery createDeliveryWithVehicleStatus(Long timestamp) {
    var vehicleStatus = new GBFSVehicleStatus();
    if (timestamp != null) {
      vehicleStatus.setLastUpdated(new Date(timestamp));
    }
    return new GbfsV3Delivery(
      null,
      null,
      null,
      null,
      null,
      null,
      vehicleStatus,
      null,
      null,
      null,
      null,
      null
    );
  }

  private GbfsV3Delivery createDeliveryWithStationStatus(Long timestamp) {
    var stationStatus = new GBFSStationStatus();
    if (timestamp != null) {
      stationStatus.setLastUpdated(new Date(timestamp));
    }
    return new GbfsV3Delivery(
      null,
      null,
      null,
      null,
      null,
      stationStatus,
      null,
      null,
      null,
      null,
      null,
      null
    );
  }
}
