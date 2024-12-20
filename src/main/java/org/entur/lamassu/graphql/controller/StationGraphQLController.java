package org.entur.lamassu.graphql.controller;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.entur.lamassu.cache.PricingPlanCache;
import org.entur.lamassu.cache.StationCache;
import org.entur.lamassu.cache.SystemCache;
import org.entur.lamassu.cache.VehicleTypeCache;
import org.entur.lamassu.model.entities.FormFactor;
import org.entur.lamassu.model.entities.PricingPlan;
import org.entur.lamassu.model.entities.PropulsionType;
import org.entur.lamassu.model.entities.Station;
import org.entur.lamassu.model.entities.System;
import org.entur.lamassu.model.entities.VehicleDocksAvailability;
import org.entur.lamassu.model.entities.VehicleType;
import org.entur.lamassu.model.entities.VehicleTypeAvailability;
import org.entur.lamassu.service.*;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

@Controller
public class StationGraphQLController extends BaseGraphQLController {

  private final GeoSearchService geoSearchService;
  private final StationCache stationCache;
  private final VehicleTypeCache vehicleTypeCache;
  private final SystemCache systemCache;
  private final PricingPlanCache pricingPlanCache;

  public StationGraphQLController(
    GeoSearchService geoSearchService,
    FeedProviderService feedProviderService,
    StationCache stationCache,
    VehicleTypeCache vehicleTypeCache,
    SystemCache systemCache,
    PricingPlanCache pricingPlanCache
  ) {
    super(feedProviderService);
    this.geoSearchService = geoSearchService;
    this.stationCache = stationCache;
    this.vehicleTypeCache = vehicleTypeCache;
    this.systemCache = systemCache;
    this.pricingPlanCache = pricingPlanCache;
  }

  @QueryMapping
  public Station station(@Argument String id) {
    return stationCache.get(id);
  }

  @QueryMapping
  public Collection<Station> stations(
    @Argument List<String> ids,
    @Argument Double lat,
    @Argument Double lon,
    @Argument Double range,
    @Argument Double minimumLatitude,
    @Argument Double minimumLongitude,
    @Argument Double maximumLatitude,
    @Argument Double maximumLongitude,
    @Argument Integer count,
    @Argument List<String> codespaces,
    @Argument List<String> systems,
    @Argument List<String> operators,
    @Argument List<FormFactor> availableFormFactors,
    @Argument List<PropulsionType> availablePropulsionTypes
  ) {
    if (ids != null && !ids.isEmpty()) {
      return stationCache.getAll(Set.copyOf(ids));
    }

    validateCount(count);
    validateCodespaces(codespaces);
    validateSystems(systems);

    var filterParams = new StationFilterParameters(
      codespaces,
      systems,
      operators,
      count,
      availableFormFactors,
      availablePropulsionTypes
    );

    Collection<Station> stations;

    validateQueryParameters(
      lat,
      lon,
      range,
      minimumLatitude,
      minimumLongitude,
      maximumLatitude,
      maximumLongitude
    );

    if (isRangeSearch(range, lat, lon)) {
      var queryParams = new RangeQueryParameters(lat, lon, range);
      stations = geoSearchService.getStationsWithinRange(queryParams, filterParams);
    } else {
      var queryParams = new BoundingBoxQueryParameters(
        minimumLatitude,
        minimumLongitude,
        maximumLatitude,
        maximumLongitude
      );
      stations = geoSearchService.getStationsInBoundingBox(queryParams, filterParams);
    }

    return stations;
  }

  @QueryMapping
  public Collection<Station> stationsById(@Argument List<String> ids) {
    return stationCache.getAll(Set.copyOf(ids));
  }

  @SchemaMapping(typeName = "VehicleTypeAvailability", field = "vehicleType")
  public VehicleType getVehicleType(VehicleTypeAvailability vehicleTypeAvailability) {
    return vehicleTypeCache.get(vehicleTypeAvailability.getVehicleTypeId());
  }

  @SchemaMapping(typeName = "VehicleDocksAvailability", field = "vehicleTypes")
  public List<VehicleType> getVehicleType(
    VehicleDocksAvailability vehicleDocksAvailability
  ) {
    return vehicleTypeCache.getAll(
      new HashSet<>(vehicleDocksAvailability.getVehicleTypeIds())
    );
  }

  @SchemaMapping(typeName = "Station", field = "system")
  public System getSystem(Station station) {
    return systemCache.get(station.getSystemId());
  }

  /**
   * GBFS does not have pricing plans directly on station. They should be resolved
   * via vehicle types instead. This is a workaround for not having to resolve
   * all of a system's pricing plans, by collecting only the pricing plan's referred
   * to by a stations various references to vehicle types
   */
  @SchemaMapping(typeName = "Station", field = "pricingPlans")
  public List<PricingPlan> getPricingPlans(Station station) {
    Set<String> pricingPlanIds = new HashSet<>();

    // example:
    station
      .getVehicleTypesAvailable()
      .stream()
      .map(this::getVehicleType)
      .forEach(vehicleType -> {
        if (vehicleType.getPricingPlanIds() != null) {
          pricingPlanIds.addAll(vehicleType.getPricingPlanIds());
        }
        if (vehicleType.getDefaultPricingPlanId() != null) {
          pricingPlanIds.add(vehicleType.getDefaultPricingPlanId());
        }
      });

    // should add from other vehicle types as well

    return pricingPlanCache.getAll(pricingPlanIds);
  }
  // TODO implement
  /*  private List<VehicleTypeCapacity> vehicleCapacity;
  private List<VehicleDocksCapacity> vehicleDocksCapacity;
  private List<VehicleTypeCapacity> vehicleTypeCapacity;
  private List<VehicleTypesCapacity> vehicleTypesCapacity; */

  // TODO: implement region resolving

  // TODO: we must also resolve pricing plans from within vehicle type
}
