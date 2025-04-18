/*
 *
 *
 *  * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 *  * the European Commission - subsequent versions of the EUPL (the "Licence");
 *  * You may not use this work except in compliance with the Licence.
 *  * You may obtain a copy of the Licence at:
 *  *
 *  *   https://joinup.ec.europa.eu/software/page/eupl
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the Licence is distributed on an "AS IS" basis,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the Licence for the specific language governing permissions and
 *  * limitations under the Licence.
 *
 */

package org.entur.lamassu.mapper.entitymapper;

import java.util.List;
import org.entur.lamassu.model.entities.Station;
import org.entur.lamassu.model.entities.Vehicle;
import org.entur.lamassu.model.entities.VehicleEquipment;
import org.mobilitydata.gbfs.v3_0.vehicle_status.GBFSVehicle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class VehicleMapper {

  private final RentalUrisMapper rentalUrisMapper;

  @Autowired
  public VehicleMapper(RentalUrisMapper rentalUrisMapper) {
    this.rentalUrisMapper = rentalUrisMapper;
  }

  public Vehicle mapVehicle(GBFSVehicle vehicle, Station station, String systemId) {
    var mappedVehicle = new Vehicle();
    mappedVehicle.setId(vehicle.getVehicleId());
    if (vehicle.getLat() == null && vehicle.getLon() == null && station != null) {
      mappedVehicle.setLat(station.getLat());
      mappedVehicle.setLon(station.getLon());
    } else {
      mappedVehicle.setLat(vehicle.getLat());
      mappedVehicle.setLon(vehicle.getLon());
    }
    mappedVehicle.setReserved(vehicle.getIsReserved());
    mappedVehicle.setDisabled(vehicle.getIsDisabled());
    mappedVehicle.setCurrentRangeMeters(
      vehicle.getCurrentRangeMeters() == null ? 0.0 : vehicle.getCurrentRangeMeters()
    );
    mappedVehicle.setCurrentFuelPercent(vehicle.getCurrentFuelPercent());
    mappedVehicle.setVehicleTypeId(vehicle.getVehicleTypeId());
    mappedVehicle.setPricingPlanId(vehicle.getPricingPlanId());
    mappedVehicle.setVehicleEquipment(mapVehicleEquipment(vehicle.getVehicleEquipment()));
    mappedVehicle.setRentalUris(rentalUrisMapper.mapRentalUris(vehicle.getRentalUris()));
    mappedVehicle.setAvailableUntil(vehicle.getAvailableUntil());
    mappedVehicle.setSystemId(systemId);
    mappedVehicle.setStationId(vehicle.getStationId());
    return mappedVehicle;
  }

  private List<VehicleEquipment> mapVehicleEquipment(
    List<org.mobilitydata.gbfs.v3_0.vehicle_status.VehicleEquipment> vehicleEquipment
  ) {
    if (vehicleEquipment == null) {
      return null;
    }

    return vehicleEquipment
      .stream()
      .map(vehicleEquipmentEnum ->
        VehicleEquipment.valueOf(vehicleEquipmentEnum.value().toUpperCase())
      )
      .toList();
  }
}
