/*
 *
 *
 *  * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 *  * the European Commission - subsequent versions of the EUPL (the "Licence");
 *  * You may not use this work except in compliance with the Licence.
 *  * You may obtain a copy of the Licence at:
 *  *
 *  *   https://joinup.ec.europa.eu/software/page/eupl
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the Licence is distributed on an "AS IS" basis,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the Licence for the specific language governing permissions and
 *  * limitations under the Licence.
 *
 */

package org.entur.lamassu.graphql.subscription.filter;

import java.util.List;
import java.util.function.UnaryOperator;
import org.entur.lamassu.graphql.subscription.model.VehicleUpdate;
import org.entur.lamassu.model.entities.FormFactor;
import org.entur.lamassu.model.entities.PropulsionType;
import org.entur.lamassu.model.entities.Vehicle;
import org.entur.lamassu.model.entities.VehicleType;
import org.entur.lamassu.service.BoundingBoxQueryParameters;
import org.entur.lamassu.service.RangeQueryParameters;
import org.entur.lamassu.service.VehicleFilterParameters;

public class VehicleUpdateFilter
  extends AbstractEntityUpdateFilter<Vehicle, VehicleFilterParameters>
  implements EntityUpdateFilter<VehicleUpdate> {

  private final VehicleFilterParameters filterParameters;

  public VehicleUpdateFilter(
    VehicleFilterParameters filterParameters,
    RangeQueryParameters rangeQueryParameters,
    UnaryOperator<String> codespaceResolver
  ) {
    super(filterParameters, rangeQueryParameters, codespaceResolver);
    this.filterParameters = filterParameters;
  }

  public VehicleUpdateFilter(
    VehicleFilterParameters filterParameters,
    BoundingBoxQueryParameters boundingBoxParameters,
    UnaryOperator<String> codespaceResolver
  ) {
    super(filterParameters, boundingBoxParameters, codespaceResolver);
    this.filterParameters = filterParameters;
  }

  @Override
  public boolean test(VehicleUpdate entity) {
    Vehicle vehicle = entity.getVehicle();

    // Check basic filter parameters
    if (doesNotMatchBasicFilters(vehicle)) {
      return false;
    }

    // Check spatial filters
    if (doesNotMatchSpatialFilters(vehicle)) {
      return false;
    }

    // Check vehicle type filters (form factor and propulsion type)
    if (!matchesVehicleTypeFilters(vehicle)) {
      return false;
    }

    // Check vehicle status filters (reserved, disabled)
    return matchesStatusFilters(vehicle);
  }

  private boolean matchesVehicleTypeFilters(Vehicle vehicle) {
    // Get the vehicle type from the vehicle
    VehicleType vehicleType = vehicle.getVehicleType();
    if (vehicleType == null) {
      // If vehicle type is null, it can't match any form factor or propulsion type filters
      return !hasAnyVehicleTypeFilters();
    }

    // Check form factor filter if specified
    List<FormFactor> formFactors = filterParameters.getFormFactors();
    if (
      hasFilterValues(formFactors) &&
      !hasMatchingAttribute(vehicleType, formFactors, VehicleType::getFormFactor)
    ) {
      return false;
    }

    // Check propulsion type filter if specified
    List<PropulsionType> propulsionTypes = filterParameters.getPropulsionTypes();
    return (
      !hasFilterValues(propulsionTypes) ||
      hasMatchingAttribute(vehicleType, propulsionTypes, VehicleType::getPropulsionType)
    );
  }

  /**
   * Checks if any vehicle type filters are specified.
   */
  private boolean hasAnyVehicleTypeFilters() {
    return (
      hasFilterValues(filterParameters.getFormFactors()) ||
      hasFilterValues(filterParameters.getPropulsionTypes())
    );
  }

  private boolean matchesStatusFilters(Vehicle vehicle) {
    // Skip status filtering if no filter parameters are set
    if (filterParameters == null) {
      return true;
    }

    // Filter by reserved status
    if (
      !filterParameters.getIncludeReserved() &&
      vehicle.getReserved() != null &&
      Boolean.TRUE.equals(vehicle.getReserved())
    ) {
      return false;
    }

    // Filter by disabled status
    return (
      filterParameters.getIncludeDisabled() ||
      vehicle.getDisabled() == null ||
      !Boolean.TRUE.equals(vehicle.getDisabled())
    );
  }
}
