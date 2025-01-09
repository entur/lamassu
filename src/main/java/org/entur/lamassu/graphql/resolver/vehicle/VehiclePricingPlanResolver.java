package org.entur.lamassu.graphql.resolver.vehicle;

import org.entur.lamassu.cache.PricingPlanCache;
import org.entur.lamassu.cache.VehicleTypeCache;
import org.entur.lamassu.model.entities.PricingPlan;
import org.entur.lamassu.model.entities.Vehicle;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

@Controller
public class VehiclePricingPlanResolver {

  private final PricingPlanCache pricingPlanCache;
  private final VehicleTypeCache vehicleTypeCache;

  public VehiclePricingPlanResolver(
    PricingPlanCache pricingPlanCache,
    VehicleTypeCache vehicleTypeCache
  ) {
    this.pricingPlanCache = pricingPlanCache;
    this.vehicleTypeCache = vehicleTypeCache;
  }

  @SchemaMapping(typeName = "Vehicle", field = "pricingPlan")
  public PricingPlan resolve(Vehicle vehicle) {
    // pricingPlanId is optional for vehicles and defaults to it's vehicleType's default pricing plan
    if (vehicle.getPricingPlanId() == null) {
      return pricingPlanCache.get(
        vehicleTypeCache.get(vehicle.getVehicleTypeId()).getDefaultPricingPlanId()
      );
    }
    return pricingPlanCache.get(vehicle.getPricingPlanId());
  }
}
