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

import java.util.function.Predicate;
import org.entur.lamassu.cache.EntityCache;
import org.entur.lamassu.model.entities.PricingPlan;
import org.entur.lamassu.model.entities.Station;
import org.entur.lamassu.model.entities.VehicleType;
import org.mobilitydata.gbfs.v3_0.vehicle_status.GBFSVehicle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
class VehicleFilter implements Predicate<GBFSVehicle> {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  private final EntityCache<PricingPlan> pricingPlanCache;
  private final EntityCache<VehicleType> vehicleTypeCache;
  private final EntityCache<Station> stationCache;

  @Value(
    "${org.entur.lamassu.vehicle-filter.include-vehicles-assigned-to-non-virtual-stations:false}"
  )
  private boolean includeVehiclesAssignedToNonVirtualStations;

  public VehicleFilter(
    EntityCache<PricingPlan> pricingPlanCache,
    EntityCache<VehicleType> vehicleTypeCache,
    EntityCache<Station> stationCache
  ) {
    this.pricingPlanCache = pricingPlanCache;
    this.vehicleTypeCache = vehicleTypeCache;
    this.stationCache = stationCache;
  }

  @Override
  public boolean test(GBFSVehicle vehicle) {
    if (vehicle.getStationId() != null) {
      var station = stationCache.get(vehicle.getStationId());
      if (
        station != null &&
        !station.getVirtualStation() &&
        !includeVehiclesAssignedToNonVirtualStations
      ) {
        logger.info(
          "Skipping vehicle {} currently assigned to non-virtual station {}",
          vehicle.getVehicleId(),
          vehicle.getStationId()
        );
        return false;
      }
    }

    if (
      vehicle.getPricingPlanId() != null &&
      !pricingPlanCache.hasKey(vehicle.getPricingPlanId())
    ) {
      logger.info(
        "Skipping vehicle with unknown pricing plan id {} (vehicle {})",
        vehicle.getPricingPlanId(),
        vehicle.getVehicleId()
      );
      return false;
    }

    if (!vehicleTypeCache.hasKey(vehicle.getVehicleTypeId())) {
      logger.info(
        "Skipping vehicle with unknown vehicle type id {} (vehicle {})",
        vehicle.getVehicleTypeId(),
        vehicle.getVehicleId()
      );
      return false;
    }

    if (
      vehicle.getPricingPlanId() == null &&
      vehicleTypeCache.get(vehicle.getVehicleTypeId()).getDefaultPricingPlanId() == null
    ) {
      logger.info(
        "Skipping vehicle without pricing plan id and vehicle type {} without default pricing plan (vehicle {})",
        vehicle.getVehicleTypeId(),
        vehicle.getVehicleId()
      );
      return false;
    }

    return true;
  }
}
