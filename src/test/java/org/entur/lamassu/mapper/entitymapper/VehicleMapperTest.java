package org.entur.lamassu.mapper.entitymapper;

import static org.junit.Assert.assertEquals;

import org.entur.lamassu.model.entities.Station;
import org.entur.lamassu.model.entities.Vehicle;
import org.junit.jupiter.api.Test;
import org.mobilitydata.gbfs.v3_0.vehicle_status.GBFSVehicle;

public class VehicleMapperTest {

  @Test
  void dockedVehicleWithoutCoordsShouldInheritStationCoords() {
    final VehicleMapper vehicleMapper = new VehicleMapper(new RentalUrisMapper());
    Station station = new Station();
    station.setId("TST:1");
    station.setLat(45.0);
    station.setLon(-110.0);

    GBFSVehicle vehicle = new GBFSVehicle().withStationId("TST:1");
    final Vehicle mappedVehicle = vehicleMapper.mapVehicle(vehicle, station, "test");
    assertEquals(station.getLat(), mappedVehicle.getLat());
    assertEquals(station.getLon(), mappedVehicle.getLon());
  }
}
