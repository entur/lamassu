package org.entur.lamassu.graphql.controller;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.entur.lamassu.cache.PricingPlanCache;
import org.entur.lamassu.cache.VehicleTypeCache;
import org.entur.lamassu.model.entities.PricingPlan;
import org.entur.lamassu.model.entities.Station;
import org.entur.lamassu.model.entities.VehicleDocksAvailability;
import org.entur.lamassu.model.entities.VehicleDocksCapacity;
import org.entur.lamassu.model.entities.VehicleType;
import org.entur.lamassu.model.entities.VehicleTypeAvailability;
import org.entur.lamassu.model.entities.VehicleTypeCapacity;
import org.entur.lamassu.model.entities.VehicleTypesCapacity;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

@Controller
public class StationPricingPlansFieldGraphQLController {

  private final VehicleTypeCache vehicleTypeCache;
  private final PricingPlanCache pricingPlanCache;

  public StationPricingPlansFieldGraphQLController(
    VehicleTypeCache vehicleTypeCache,
    PricingPlanCache pricingPlanCache
  ) {
    this.vehicleTypeCache = vehicleTypeCache;
    this.pricingPlanCache = pricingPlanCache;
  }

  /**
   * GBFS does not have pricing plans directly on station. They should be resolved
   * via vehicle types instead. This is a workaround for not having to resolve
   * all of a system's pricing plans, by collecting only the pricing plan's referred
   * to by a stations various references to vehicle types
   */
  @SchemaMapping(typeName = "Station", field = "pricingPlans")
  public List<PricingPlan> getPricingPlans(Station station) {
    Set<String> vehicleTypeIds = Stream
      .of(
        Optional
          .ofNullable(station.getVehicleCapacity())
          .orElse(List.of())
          .stream()
          .map(VehicleTypeCapacity::getVehicleTypeId),
        Optional
          .ofNullable(station.getVehicleDocksCapacity())
          .orElse(List.of())
          .stream()
          .map(VehicleDocksCapacity::getVehicleTypeIds)
          .flatMap(Collection::stream),
        Optional
          .ofNullable(station.getVehicleTypeCapacity())
          .orElse(List.of())
          .stream()
          .map(VehicleTypeCapacity::getVehicleTypeId),
        Optional
          .ofNullable(station.getVehicleTypesCapacity())
          .orElse(List.of())
          .stream()
          .map(VehicleTypesCapacity::getVehicleTypeIds)
          .flatMap(Collection::stream),
        Optional
          .ofNullable(station.getVehicleTypesAvailable())
          .orElse(List.of())
          .stream()
          .map(VehicleTypeAvailability::getVehicleTypeId),
        Optional
          .ofNullable(station.getVehicleDocksAvailable())
          .orElse(List.of())
          .stream()
          .map(VehicleDocksAvailability::getVehicleTypeIds)
          .flatMap(Collection::stream)
      )
      .flatMap(i -> i)
      .collect(Collectors.toSet());

    List<VehicleType> vehicleTypes = vehicleTypeCache.getAll(vehicleTypeIds);

    Set<String> pricingPlanIds = new HashSet<>();

    vehicleTypes.forEach(vehicleType -> {
      if (vehicleType.getPricingPlanIds() != null) {
        pricingPlanIds.addAll(vehicleType.getPricingPlanIds());
      }
      if (vehicleType.getDefaultPricingPlanId() != null) {
        pricingPlanIds.add(vehicleType.getDefaultPricingPlanId());
      }
    });

    return pricingPlanCache.getAll(pricingPlanIds);
  }
}
