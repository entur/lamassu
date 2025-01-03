package org.entur.lamassu.graphql.controller;

import java.util.List;
import java.util.stream.Collectors;
import org.entur.lamassu.cache.PricingPlanCache;
import org.entur.lamassu.model.entities.PricingPlan;
import org.entur.lamassu.model.entities.VehicleType;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

@Controller
public class VehicleTypeGraphQLController {

  private final PricingPlanCache pricingPlanCache;

  public VehicleTypeGraphQLController(PricingPlanCache pricingPlanCache) {
    this.pricingPlanCache = pricingPlanCache;
  }

  @SchemaMapping(typeName = "VehicleType", field = "defaultPricingPlan")
  public PricingPlan pricingPlan(VehicleType vehicleType) {
    return pricingPlanCache.get(vehicleType.getDefaultPricingPlanId());
  }

  @SchemaMapping(typeName = "VehicleType", field = "pricingPlans")
  public List<PricingPlan> pricingPlans(VehicleType vehicleType) {
    return pricingPlanCache.getAll(
      vehicleType.getPricingPlanIds().stream().collect(Collectors.toSet())
    );
  }
}
