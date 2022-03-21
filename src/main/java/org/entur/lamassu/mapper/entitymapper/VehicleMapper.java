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

import org.entur.gbfs.v2_3.free_bike_status.GBFSBike;
import org.entur.lamassu.model.entities.PricingPlan;
import org.entur.lamassu.model.entities.System;
import org.entur.lamassu.model.entities.Vehicle;
import org.entur.lamassu.model.entities.VehicleType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class VehicleMapper {
    private final RentalUrisMapper rentalUrisMapper;

    @Autowired
    public VehicleMapper(RentalUrisMapper rentalUrisMapper) {
        this.rentalUrisMapper = rentalUrisMapper;
    }

    public Vehicle mapVehicle(GBFSBike bike, VehicleType vehicleType, PricingPlan pricingPlan, System system) {
        var vehicle = new Vehicle();
        vehicle.setId(bike.getBikeId());
        vehicle.setLat(bike.getLat());
        vehicle.setLon(bike.getLon());
        vehicle.setReserved(bike.getIsReserved());
        vehicle.setDisabled(bike.getIsDisabled());
        vehicle.setCurrentRangeMeters(bike.getCurrentRangeMeters());
        vehicle.setVehicleType(vehicleType);
        vehicle.setPricingPlan(pricingPlan);
        vehicle.setRentalUris(rentalUrisMapper.mapRentalUris(bike.getRentalUris()));
        vehicle.setSystem(system);
        return vehicle;
    }
}
