package org.entur.lamassu.leader.entityupdater;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;
import org.entur.gbfs.loader.v3.GbfsV3Delivery;
import org.entur.lamassu.leader.GbfsUpdateContinuityTracker;
import org.entur.lamassu.stubs.StubUpdateContinuityCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mobilitydata.gbfs.v3_0.station_status.GBFSStationStatus;
import org.mobilitydata.gbfs.v3_0.vehicle_status.GBFSVehicleStatus;

class GbfsUpdateContinuityTrackerTest {

  private static final String SYSTEM_ID = "test-system";
  private GbfsUpdateContinuityTracker tracker;
  private StubUpdateContinuityCache vehicleCache;
  private StubUpdateContinuityCache stationCache;

  @BeforeEach
  void setUp() {
    vehicleCache = new StubUpdateContinuityCache();
    stationCache = new StubUpdateContinuityCache();
    tracker = new GbfsUpdateContinuityTracker(vehicleCache, stationCache);
  }

  @Test
  void hasVehicleUpdateContinuity_shouldReturnFalse_whenOldDeliveryIsNull() {
    var oldDelivery = createDeliveryWithVehicleStatus(null);
    assertFalse(tracker.hasVehicleUpdateContinuity(SYSTEM_ID, oldDelivery));
  }

  @Test
  void hasVehicleUpdateContinuity_shouldReturnFalse_whenFirstUpdate() {
    var oldDelivery = createDeliveryWithVehicleStatus(500L);
    assertFalse(tracker.hasVehicleUpdateContinuity(SYSTEM_ID, oldDelivery));
  }

  @Test
  void hasVehicleUpdateContinuity_shouldReturnTrue_whenTimestampsMatch() {
    var delivery = createDeliveryWithVehicleStatus(1000L);

    // First update establishes the base
    tracker.updateVehicleUpdateContinuity(SYSTEM_ID, delivery);

    // Check continuity with matching timestamp
    assertTrue(tracker.hasVehicleUpdateContinuity(SYSTEM_ID, delivery));
  }

  @Test
  void hasVehicleUpdateContinuity_shouldReturnFalse_whenTimestampsDontMatch() {
    var delivery = createDeliveryWithVehicleStatus(1000L);

    // First update establishes the base
    tracker.updateVehicleUpdateContinuity(SYSTEM_ID, delivery);

    // Check with non-matching timestamps (simulating missed update)
    var modifiedDelivery = createDeliveryWithVehicleStatus(800L);
    assertFalse(tracker.hasVehicleUpdateContinuity(SYSTEM_ID, modifiedDelivery));
  }

  @Test
  void hasStationUpdateContinuity_shouldReturnFalse_whenOldDeliveryIsNull() {
    var oldDelivery = createDeliveryWithStationStatus(null);
    assertFalse(tracker.hasStationUpdateContinuity(SYSTEM_ID, oldDelivery));
  }

  @Test
  void hasStationUpdateContinuity_shouldReturnFalse_whenFirstUpdate() {
    var oldDelivery = createDeliveryWithStationStatus(500L);
    assertFalse(tracker.hasStationUpdateContinuity(SYSTEM_ID, oldDelivery));
  }

  @Test
  void hasStationUpdateContinuity_shouldReturnTrue_whenTimestampsMatch() {
    var delivery = createDeliveryWithStationStatus(1000L);

    // First update establishes the base
    tracker.updateStationUpdateContinuity(SYSTEM_ID, delivery);

    // Check continuity with matching timestamp
    assertTrue(tracker.hasStationUpdateContinuity(SYSTEM_ID, delivery));
  }

  @Test
  void hasStationUpdateContinuity_shouldReturnFalse_whenTimestampsDontMatch() {
    var delivery = createDeliveryWithStationStatus(1000L);

    // First update establishes the base
    tracker.updateStationUpdateContinuity(SYSTEM_ID, delivery);

    // Check with non-matching timestamps (simulating missed update)
    var modifiedDelivery = createDeliveryWithStationStatus(800L);
    assertFalse(tracker.hasStationUpdateContinuity(SYSTEM_ID, modifiedDelivery));
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
