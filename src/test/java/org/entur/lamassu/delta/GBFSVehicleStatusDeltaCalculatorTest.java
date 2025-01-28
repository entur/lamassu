package org.entur.lamassu.delta;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mobilitydata.gbfs.v3_0.vehicle_status.GBFSData;
import org.mobilitydata.gbfs.v3_0.vehicle_status.GBFSVehicle;
import org.mobilitydata.gbfs.v3_0.vehicle_status.GBFSVehicleStatus;

class GBFSVehicleStatusDeltaCalculatorTest {

  private final GBFSVehicleStatusDeltaCalculator calculator =
    new GBFSVehicleStatusDeltaCalculator();

  private GBFSVehicleStatus createFeed(
    long lastUpdated,
    int ttl,
    List<GBFSVehicle> vehicles
  ) {
    var data = new GBFSData();
    data.setVehicles(vehicles);
    var feed = new GBFSVehicleStatus();
    feed.setLastUpdated(new Date(lastUpdated));
    feed.setTtl(ttl);
    feed.setData(data);
    return feed;
  }

  private GBFSVehicle createVehicle(
    String id,
    Double lat,
    Double lon,
    Double currentRangeMeters
  ) {
    var vehicle = new GBFSVehicle();
    vehicle.setVehicleId(id);
    vehicle.setLat(lat);
    vehicle.setLon(lon);
    vehicle.setCurrentRangeMeters(currentRangeMeters);
    return vehicle;
  }

  @Test
  void shouldDetectMovedVehicle() {
    var base = createFeed(1000L, 60, List.of(createVehicle("1", 59.9, 10.7, 80.0)));

    var compare = createFeed(2000L, 60, List.of(createVehicle("1", 59.91, 10.71, 80.0)));

    var delta = calculator.calculateDelta(base, compare);

    assertEquals(1, delta.entityDelta().size());
    var entityDelta = delta.entityDelta().getFirst();
    assertEquals("1", entityDelta.entityId());
    assertEquals(DeltaType.UPDATE, entityDelta.type());
    assertEquals(59.91, entityDelta.entity().getLat());
    assertEquals(10.71, entityDelta.entity().getLon());
  }

  @Test
  void shouldDetectNewVehicle() {
    var base = createFeed(1000L, 60, List.of());

    var compare = createFeed(2000L, 60, List.of(createVehicle("1", 59.9, 10.7, 8000.0)));

    var delta = calculator.calculateDelta(base, compare);

    assertEquals(1, delta.entityDelta().size());
    var entityDelta = delta.entityDelta().getFirst();
    assertEquals("1", entityDelta.entityId());
    assertEquals(DeltaType.CREATE, entityDelta.type());
    assertEquals(59.9, entityDelta.entity().getLat());
    assertEquals(10.7, entityDelta.entity().getLon());
    assertEquals(8000, entityDelta.entity().getCurrentRangeMeters());
  }

  @Test
  void shouldDetectRemovedVehicle() {
    var base = createFeed(1000L, 60, List.of(createVehicle("1", 59.9, 10.7, 8000.0)));

    var compare = createFeed(2000L, 60, List.of());

    var delta = calculator.calculateDelta(base, compare);

    assertEquals(1, delta.entityDelta().size());
    var entityDelta = delta.entityDelta().getFirst();
    assertEquals("1", entityDelta.entityId());
    assertEquals(DeltaType.DELETE, entityDelta.type());
    assertNull(entityDelta.entity());
  }

  @Test
  void shouldNotCreateDeltaForUnchangedVehicle() {
    var base = createFeed(1000L, 60, List.of(createVehicle("1", 59.9, 10.7, 8000.0)));

    var compare = createFeed(2000L, 60, List.of(createVehicle("1", 59.9, 10.7, 8000.0)));

    var delta = calculator.calculateDelta(base, compare);

    assertTrue(delta.entityDelta().isEmpty());
  }

  @Test
  void shouldHandlePartialUpdates() {
    var base = createFeed(1000L, 60, List.of(createVehicle("1", 59.9, 10.7, 8000.0)));

    var updatedVehicle = createVehicle("1", 59.9, 10.7, 7500.0);
    updatedVehicle.setIsReserved(true); // Add a new field

    var compare = createFeed(2000L, 60, List.of(updatedVehicle));

    var delta = calculator.calculateDelta(base, compare);

    assertEquals(1, delta.entityDelta().size());
    var entityDelta = delta.entityDelta().getFirst();
    assertEquals(7500, entityDelta.entity().getCurrentRangeMeters());
    assertTrue(entityDelta.entity().getIsReserved()); // New field should be included
  }
}
