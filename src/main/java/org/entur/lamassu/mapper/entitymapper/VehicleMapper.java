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
import java.util.stream.Collectors;
import org.entur.lamassu.model.entities.PricingPlan;
import org.entur.lamassu.model.entities.System;
import org.entur.lamassu.model.entities.Vehicle;
import org.entur.lamassu.model.entities.VehicleEquipment;
import org.entur.lamassu.model.entities.VehicleType;
import org.mobilitydata.gbfs.v2_3.free_bike_status.GBFSBike;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class VehicleMapper {

  private final RentalUrisMapper rentalUrisMapper;

  @Autowired
  public VehicleMapper(RentalUrisMapper rentalUrisMapper) {
    this.rentalUrisMapper = rentalUrisMapper;
  }

  public Vehicle mapVehicle(
    GBFSBike bike,
    VehicleType vehicleType,
    PricingPlan pricingPlan,
    System system
  ) {
    var vehicle = new Vehicle();
    vehicle.setId(bike.getBikeId());
    vehicle.setLat(bike.getLat());
    vehicle.setLon(bike.getLon());
    vehicle.setReserved(bike.getIsReserved());
    vehicle.setDisabled(bike.getIsDisabled());
    vehicle.setCurrentRangeMeters(bike.getCurrentRangeMeters());
    vehicle.setCurrentFuelPercent(bike.getCurrentFuelPercent());
    vehicle.setVehicleType(vehicleType);
    vehicle.setPricingPlan(pricingPlan);
    vehicle.setVehicleEquipment(mapVehicleEquipment(bike.getVehicleEquipment()));
    vehicle.setRentalUris(rentalUrisMapper.mapRentalUris(bike.getRentalUris()));
    vehicle.setAvailableUntil(bike.getAvailableUntil());
    vehicle.setSystem(system);
    return vehicle;
  }

  private List<VehicleEquipment> mapVehicleEquipment(
    List<org.mobilitydata.gbfs.v2_3.free_bike_status.VehicleEquipment> vehicleEquipment
  ) {
    if (vehicleEquipment == null) {
      return null;
    }

    return vehicleEquipment
      .stream()
      .map(vehicleEquipmentEnum ->
        VehicleEquipment.valueOf(vehicleEquipmentEnum.value().toUpperCase())
      )
      .collect(Collectors.toList());
  }
}
