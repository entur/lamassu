package org.entur.lamassu.graphql.controller;

import org.entur.lamassu.cache.PricingPlanCache;
import org.entur.lamassu.model.entities.PricingPlan;
import org.entur.lamassu.model.entities.Vehicle;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

@Controller
public class VehiclePricingPlanFieldGraphQLController {

  private final PricingPlanCache pricingPlanCache;

  public VehiclePricingPlanFieldGraphQLController(PricingPlanCache pricingPlanCache) {
    this.pricingPlanCache = pricingPlanCache;
  }

  @SchemaMapping(typeName = "Vehicle", field = "pricingPlan")
  public PricingPlan pricingPlan(Vehicle vehicle) {
    return pricingPlanCache.get(vehicle.getPricingPlanId());
  }
}
