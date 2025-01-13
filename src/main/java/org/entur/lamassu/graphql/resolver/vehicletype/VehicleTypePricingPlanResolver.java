package org.entur.lamassu.graphql.resolver.vehicletype;

import java.util.List;
import java.util.stream.Collectors;
import org.entur.lamassu.cache.EntityReader;
import org.entur.lamassu.model.entities.PricingPlan;
import org.entur.lamassu.model.entities.VehicleType;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

@Controller
public class VehicleTypePricingPlanResolver {

  private final EntityReader<PricingPlan> pricingPlanReader;

  public VehicleTypePricingPlanResolver(EntityReader<PricingPlan> pricingPlanReader) {
    this.pricingPlanReader = pricingPlanReader;
  }

  @SchemaMapping(typeName = "VehicleType", field = "defaultPricingPlan")
  public PricingPlan resolve(VehicleType vehicleType) {
    if (vehicleType.getDefaultPricingPlanId() == null) {
      return null;
    }
    return pricingPlanReader.get(vehicleType.getDefaultPricingPlanId());
  }

  @SchemaMapping(typeName = "VehicleType", field = "pricingPlans")
  public List<PricingPlan> pricingPlans(VehicleType vehicleType) {
    if (vehicleType.getPricingPlanIds() == null) {
      return null;
    }
    return vehicleType
      .getPricingPlanIds()
      .stream()
      .map(pricingPlanReader::get)
      .collect(Collectors.toList());
  }
}
