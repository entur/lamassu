package org.entur.lamassu.graphql.resolver.vehicle;

import org.entur.lamassu.cache.PricingPlanCache;
import org.entur.lamassu.model.entities.PricingPlan;
import org.entur.lamassu.model.entities.Vehicle;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

@Controller
public class VehiclePricingPlanResolver {

  private final PricingPlanCache pricingPlanCache;

  public VehiclePricingPlanResolver(PricingPlanCache pricingPlanCache) {
    this.pricingPlanCache = pricingPlanCache;
  }

  @SchemaMapping(typeName = "Vehicle", field = "pricingPlan")
  public PricingPlan resolve(Vehicle vehicle) {
    return pricingPlanCache.get(vehicle.getPricingPlanId());
  }
}
