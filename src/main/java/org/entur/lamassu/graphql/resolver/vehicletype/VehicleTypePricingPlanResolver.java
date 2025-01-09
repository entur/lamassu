package org.entur.lamassu.graphql.resolver.vehicletype;

import java.util.List;
import java.util.stream.Collectors;
import org.entur.lamassu.cache.PricingPlanCache;
import org.entur.lamassu.model.entities.PricingPlan;
import org.entur.lamassu.model.entities.VehicleType;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

@Controller
public class VehicleTypePricingPlanResolver {

  private final PricingPlanCache pricingPlanCache;

  public VehicleTypePricingPlanResolver(PricingPlanCache pricingPlanCache) {
    this.pricingPlanCache = pricingPlanCache;
  }

  @SchemaMapping(typeName = "VehicleType", field = "defaultPricingPlan")
  public PricingPlan defaultPricingPlan(VehicleType vehicleType) {
    if (vehicleType.getDefaultPricingPlanId() == null) {
      return null;
    }
    return pricingPlanCache.get(vehicleType.getDefaultPricingPlanId());
  }

  @SchemaMapping(typeName = "VehicleType", field = "pricingPlans")
  public List<PricingPlan> pricingPlans(VehicleType vehicleType) {
    if (vehicleType.getPricingPlanIds() == null) {
      return null;
    }
    return pricingPlanCache.getAll(
      vehicleType.getPricingPlanIds().stream().collect(Collectors.toSet())
    );
  }
}
