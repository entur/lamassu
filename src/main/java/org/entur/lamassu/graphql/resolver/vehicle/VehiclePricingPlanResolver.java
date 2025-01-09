package org.entur.lamassu.graphql.resolver.vehicle;

import org.entur.lamassu.cache.EntityCache;
import org.entur.lamassu.model.entities.PricingPlan;
import org.entur.lamassu.model.entities.Vehicle;
import org.entur.lamassu.model.entities.VehicleType;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

@Controller
public class VehiclePricingPlanResolver {

  private final EntityCache<PricingPlan> pricingPlanCache;
  private final EntityCache<VehicleType> vehicleTypeCache;

  public VehiclePricingPlanResolver(
    EntityCache<PricingPlan> pricingPlanCache,
    EntityCache<VehicleType> vehicleTypeCache
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
