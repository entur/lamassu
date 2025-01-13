package org.entur.lamassu.graphql.query;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import org.entur.lamassu.cache.EntityReader;
import org.entur.lamassu.graphql.validation.QueryParameterValidator;
import org.entur.lamassu.model.entities.FormFactor;
import org.entur.lamassu.model.entities.PropulsionType;
import org.entur.lamassu.model.entities.Vehicle;
import org.entur.lamassu.service.BoundingBoxQueryParameters;
import org.entur.lamassu.service.GeoSearchService;
import org.entur.lamassu.service.RangeQueryParameters;
import org.entur.lamassu.service.VehicleFilterParameters;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class VehicleQueryController {

  private final GeoSearchService geoSearchService;
  private final EntityReader<Vehicle> vehicleReader;
  private final QueryParameterValidator validationService;

  public VehicleQueryController(
    GeoSearchService geoSearchService,
    EntityReader<Vehicle> vehicleReader,
    QueryParameterValidator validationService
  ) {
    this.geoSearchService = geoSearchService;
    this.vehicleReader = vehicleReader;
    this.validationService = validationService;
  }

  @QueryMapping
  public Vehicle vehicle(@Argument String id) {
    return vehicleReader.get(id);
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
      return vehicleReader.getAll(new HashSet<>(ids));
    }

    validationService.validateCount(count);
    validationService.validateCodespaces(codespaces);
    validationService.validateSystems(systems);

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

    validationService.validateQueryParameters(
      lat,
      lon,
      range,
      minimumLatitude,
      minimumLongitude,
      maximumLatitude,
      maximumLongitude
    );

    if (validationService.isRangeSearch(range, lat, lon)) {
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

    if (count != null) {
      return vehicles.stream().limit(count).toList();
    }

    return vehicles;
  }
}
