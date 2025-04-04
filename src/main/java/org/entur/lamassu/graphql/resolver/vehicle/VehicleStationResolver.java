package org.entur.lamassu.graphql.resolver.vehicle;

import org.entur.lamassu.cache.EntityReader;
import org.entur.lamassu.model.entities.Station;
import org.entur.lamassu.model.entities.Vehicle;
import org.entur.lamassu.model.entities.VehicleType;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

@Controller
public class VehicleStationResolver {

  private final EntityReader<Station> stationReader;

  public VehicleStationResolver(EntityReader<Station> stationReader) {
    this.stationReader = stationReader;
  }

  @SchemaMapping(typeName = "Vehicle", field = "station")
  public Station resolve(Vehicle vehicle) {
    if (vehicle.getStationId() == null) {
      return null;
    }
    return stationReader.get(vehicle.getStationId());
  }
}
