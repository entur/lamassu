package org.entur.lamassu.delta;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mobilitydata.gbfs.v3_0.station_status.GBFSData;
import org.mobilitydata.gbfs.v3_0.station_status.GBFSStation;
import org.mobilitydata.gbfs.v3_0.station_status.GBFSStationStatus;

class GBFSStationStatusDeltaCalculatorTest {

  private final GBFSStationStatusDeltaCalculator calculator =
    new GBFSStationStatusDeltaCalculator();

  private GBFSStationStatus createFeed(
    long lastUpdated,
    int ttl,
    List<GBFSStation> stations
  ) {
    var data = new GBFSData();
    data.setStations(stations);
    var feed = new GBFSStationStatus();
    feed.setLastUpdated(new Date(lastUpdated));
    feed.setTtl(ttl);
    feed.setData(data);
    return feed;
  }

  private GBFSStation createStation(
    String id,
    Integer numVehiclesAvailable,
    Integer numDocksAvailable
  ) {
    var station = new GBFSStation();
    station.setStationId(id);
    station.setNumVehiclesAvailable(numVehiclesAvailable);
    station.setNumDocksAvailable(numDocksAvailable);
    return station;
  }

  @Test
  void shouldDetectChangedStationStatus() {
    var base = createFeed(1000L, 60, List.of(createStation("1", 5, 10)));

    var compare = createFeed(2000L, 60, List.of(createStation("1", 4, 11)));

    var delta = calculator.calculateDelta(base, compare);

    assertEquals(1, delta.entityDelta().size());
    var entityDelta = delta.entityDelta().getFirst();
    assertEquals("1", entityDelta.entityId());
    assertEquals(DeltaType.UPDATE, entityDelta.type());
    assertEquals(4, entityDelta.entity().getNumVehiclesAvailable());
    assertEquals(11, entityDelta.entity().getNumDocksAvailable());
  }

  @Test
  void shouldDetectNewStation() {
    var base = createFeed(1000L, 60, List.of());

    var compare = createFeed(2000L, 60, List.of(createStation("1", 5, 10)));

    var delta = calculator.calculateDelta(base, compare);

    assertEquals(1, delta.entityDelta().size());
    var entityDelta = delta.entityDelta().getFirst();
    assertEquals("1", entityDelta.entityId());
    assertEquals(DeltaType.CREATE, entityDelta.type());
    assertEquals(5, entityDelta.entity().getNumVehiclesAvailable());
    assertEquals(10, entityDelta.entity().getNumDocksAvailable());
  }

  @Test
  void shouldDetectRemovedStation() {
    var base = createFeed(1000L, 60, List.of(createStation("1", 5, 10)));

    var compare = createFeed(2000L, 60, List.of());

    var delta = calculator.calculateDelta(base, compare);

    assertEquals(1, delta.entityDelta().size());
    var entityDelta = delta.entityDelta().getFirst();
    assertEquals("1", entityDelta.entityId());
    assertEquals(DeltaType.DELETE, entityDelta.type());
    assertNull(entityDelta.entity());
  }

  @Test
  void shouldNotCreateDeltaForUnchangedStation() {
    var base = createFeed(1000L, 60, List.of(createStation("1", 5, 10)));

    var compare = createFeed(2000L, 60, List.of(createStation("1", 5, 10)));

    var delta = calculator.calculateDelta(base, compare);

    assertTrue(delta.entityDelta().isEmpty());
  }

  @Test
  void shouldHandlePartialUpdates() {
    var base = createFeed(1000L, 60, List.of(createStation("1", 5, 10)));

    var updatedStation = createStation("1", 4, 10);
    updatedStation.setIsInstalled(false); // Add a new field

    var compare = createFeed(2000L, 60, List.of(updatedStation));

    var delta = calculator.calculateDelta(base, compare);

    assertEquals(1, delta.entityDelta().size());
    var entityDelta = delta.entityDelta().getFirst();
    assertEquals(4, entityDelta.entity().getNumVehiclesAvailable());
    assertFalse(entityDelta.entity().getIsInstalled()); // New field should be included
  }
}
