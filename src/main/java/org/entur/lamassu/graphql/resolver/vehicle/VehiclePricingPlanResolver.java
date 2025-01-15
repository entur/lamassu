package org.entur.lamassu.graphql.resolver.vehicle;

import org.entur.lamassu.cache.EntityReader;
import org.entur.lamassu.model.entities.PricingPlan;
import org.entur.lamassu.model.entities.Vehicle;
import org.entur.lamassu.model.entities.VehicleType;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

@Controller
public class VehiclePricingPlanResolver {

  private final EntityReader<PricingPlan> pricingPlanReader;
  private final EntityReader<VehicleType> vehicleTypeReader;

  public VehiclePricingPlanResolver(
    EntityReader<PricingPlan> pricingPlanReader,
    EntityReader<VehicleType> vehicleTypeReader
  ) {
    this.pricingPlanReader = pricingPlanReader;
    this.vehicleTypeReader = vehicleTypeReader;
  }

  @SchemaMapping(typeName = "Vehicle", field = "pricingPlan")
  public PricingPlan resolve(Vehicle vehicle) {
    // pricingPlanId is optional for vehicles and defaults to it's vehicleType's default pricing plan
    if (vehicle.getPricingPlanId() == null) {
      return pricingPlanReader.get(
        vehicleTypeReader.get(vehicle.getVehicleTypeId()).getDefaultPricingPlanId()
      );
    }
    return pricingPlanReader.get(vehicle.getPricingPlanId());
  }
}
