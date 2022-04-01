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

package org.entur.lamassu.leader.entityupdater;

import org.entur.gbfs.v2_3.free_bike_status.GBFSBike;
import org.entur.lamassu.model.entities.PricingPlan;
import org.entur.lamassu.model.entities.VehicleType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.function.Predicate;

class VehicleFilter implements Predicate<GBFSBike> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Map<String, PricingPlan> pricingPlans;
    private final Map<String, VehicleType> vehicleTypes;

    public VehicleFilter(Map<String, PricingPlan> pricingPlans, Map<String, VehicleType> vehicleTypes) {
        this.pricingPlans = pricingPlans;
        this.vehicleTypes = vehicleTypes;
    }

    @Override
    public boolean test(GBFSBike vehicle) {
        if (vehicle.getStationId() != null) {
            logger.debug("Skipping hybrid-system vehicle {}", vehicle);
            return false;
        }

        if (!pricingPlans.containsKey(vehicle.getPricingPlanId())) {
            logger.warn("Skipping vehicle with unknown pricing plan id {}", vehicle);
            return false;
        }

        if (!vehicleTypes.containsKey(vehicle.getVehicleTypeId())) {
            logger.warn("Skipping vehicle with unknown vehicle type id {}", vehicle);
            return false;
        }

        return true;
    }
}
