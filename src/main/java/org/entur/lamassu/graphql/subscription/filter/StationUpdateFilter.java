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
import java.util.function.Function;
import java.util.function.UnaryOperator;
import org.entur.lamassu.graphql.subscription.model.StationUpdate;
import org.entur.lamassu.model.entities.FormFactor;
import org.entur.lamassu.model.entities.PropulsionType;
import org.entur.lamassu.model.entities.Station;
import org.entur.lamassu.model.entities.VehicleType;
import org.entur.lamassu.model.entities.VehicleTypeAvailability;
import org.entur.lamassu.service.BoundingBoxQueryParameters;
import org.entur.lamassu.service.RangeQueryParameters;
import org.entur.lamassu.service.StationFilterParameters;

public class StationUpdateFilter
  extends AbstractEntityUpdateFilter<Station, StationFilterParameters>
  implements EntityUpdateFilter<StationUpdate> {

  private final StationFilterParameters filterParameters;

  public StationUpdateFilter(
    StationFilterParameters filterParameters,
    RangeQueryParameters rangeQueryParameters,
    UnaryOperator<String> codespaceResolver
  ) {
    super(filterParameters, rangeQueryParameters, codespaceResolver);
    this.filterParameters = filterParameters;
  }

  public StationUpdateFilter(
    StationFilterParameters filterParameters,
    BoundingBoxQueryParameters boundingBoxParameters,
    UnaryOperator<String> codespaceResolver
  ) {
    super(filterParameters, boundingBoxParameters, codespaceResolver);
    this.filterParameters = filterParameters;
  }

  @Override
  public boolean test(StationUpdate entity) {
    Station station = entity.getStation();

    // Check basic filter parameters
    if (doesNotMatchBasicFilters(station)) {
      return false;
    }

    // Check spatial filters
    if (doesNotMatchSpatialFilters(station)) {
      return false;
    }

    // Check form factor and propulsion type filters
    return matchesVehicleTypeFilters(station);
  }

  private boolean matchesVehicleTypeFilters(Station station) {
    // Get vehicle types available from the station
    List<VehicleTypeAvailability> vehicleTypesAvailable =
      station.getVehicleTypesAvailable();

    // Check form factor filter if specified
    List<FormFactor> availableFormFactors = filterParameters.getAvailableFormFactors();
    if (
      hasFilterValues(availableFormFactors) &&
      hasNoMatchingVehicleTypeAttribute(
        vehicleTypesAvailable,
        availableFormFactors,
        VehicleType::getFormFactor
      )
    ) {
      return false;
    }

    // Check propulsion type filter if specified
    List<PropulsionType> availablePropulsionTypes =
      filterParameters.getAvailablePropulsionTypes();
    return (
      !hasFilterValues(availablePropulsionTypes) ||
      !hasNoMatchingVehicleTypeAttribute(
        vehicleTypesAvailable,
        availablePropulsionTypes,
        VehicleType::getPropulsionType
      )
    );
  }

  /**
   * Checks if any vehicle type availability matches the specified attribute values.
   *
   * @param vehicleTypesAvailable The list of vehicle type availabilities to check
   * @param attributeValues The list of attribute values to match against
   * @param attributeExtractor Function to extract the attribute from a vehicle type
   * @return true if at least one vehicle type has a matching attribute, false otherwise
   */
  private <T> boolean hasNoMatchingVehicleTypeAttribute(
    List<VehicleTypeAvailability> vehicleTypesAvailable,
    List<T> attributeValues,
    Function<VehicleType, T> attributeExtractor
  ) {
    // If no vehicle types are available, we can't match any attributes
    if (vehicleTypesAvailable == null || vehicleTypesAvailable.isEmpty()) {
      return true;
    }

    // Check each vehicle type for a matching attribute
    for (VehicleTypeAvailability typeAvailability : vehicleTypesAvailable) {
      VehicleType vehicleType = typeAvailability.getVehicleType();
      if (vehicleType == null) {
        continue;
      }

      if (hasMatchingAttribute(vehicleType, attributeValues, attributeExtractor)) {
        return false;
      }
    }

    return true;
  }
}
