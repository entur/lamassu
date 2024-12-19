package org.entur.lamassu.graphql.controller;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.entur.lamassu.cache.VehicleCache;
import org.entur.lamassu.model.entities.FormFactor;
import org.entur.lamassu.model.entities.PropulsionType;
import org.entur.lamassu.model.entities.Vehicle;
import org.entur.lamassu.service.BoundingBoxQueryParameters;
import org.entur.lamassu.service.FeedProviderService;
import org.entur.lamassu.service.GeoSearchService;
import org.entur.lamassu.service.RangeQueryParameters;
import org.entur.lamassu.service.VehicleFilterParameters;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class VehicleGraphQLController extends BaseGraphQLController {

  private final GeoSearchService geoSearchService;
  private final VehicleCache vehicleCache;

  public VehicleGraphQLController(
    GeoSearchService geoSearchService,
    FeedProviderService feedProviderService,
    VehicleCache vehicleCache
  ) {
    super(feedProviderService);
    this.geoSearchService = geoSearchService;
    this.vehicleCache = vehicleCache;
  }

  @QueryMapping
  public Vehicle vehicle(@Argument String id) {
    return vehicleCache.get(id);
  }

  @QueryMapping
  public Collection<Vehicle> vehicles(
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
    @Argument List<FormFactor> formFactors,
    @Argument List<PropulsionType> propulsionTypes,
    @Argument Boolean includeReserved,
    @Argument Boolean includeDisabled
  ) {
    if (ids != null && !ids.isEmpty()) {
      return vehicleCache.getAll(Set.copyOf(ids));
    }

    validateCount(count);
    validateCodespaces(codespaces);
    validateSystems(systems);

    var filterParams = new VehicleFilterParameters(
      codespaces,
      systems,
      operators,
      count,
      formFactors,
      propulsionTypes,
      includeReserved,
      includeDisabled
    );

    Collection<Vehicle> vehicles;

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
      vehicles = geoSearchService.getVehiclesWithinRange(queryParams, filterParams);
    } else {
      var queryParams = new BoundingBoxQueryParameters(
        minimumLatitude,
        minimumLongitude,
        maximumLatitude,
        maximumLongitude
      );
      vehicles = geoSearchService.getVehiclesInBoundingBox(queryParams, filterParams);
    }

    return vehicles;
  }
}
