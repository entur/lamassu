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
import org.entur.lamassu.model.entities.FormFactor;
import org.entur.lamassu.model.entities.PropulsionType;
import org.entur.lamassu.model.entities.Station;
import org.entur.lamassu.model.entities.VehicleTypeAvailability;
import org.entur.lamassu.graphql.subscription.model.StationUpdate;
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
    if (entity == null || entity.getStation() == null) {
      return false;
    }

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
    // Skip vehicle type filtering if no filter parameters are set
    if (filterParameters == null) {
      return true;
    }

    // Filter by available form factors
    List<FormFactor> availableFormFactors = filterParameters.getAvailableFormFactors();
    if (availableFormFactors != null && !availableFormFactors.isEmpty()) {
      // Check if station has vehicle types available with the requested form factors
      List<VehicleTypeAvailability> vehicleTypesAvailable =
        station.getVehicleTypesAvailable();

      // If no vehicle types are available, we can't filter by form factor
      if (vehicleTypesAvailable == null || vehicleTypesAvailable.isEmpty()) {
        return false;
      }

      boolean hasMatchingFormFactor = false;
      for (VehicleTypeAvailability typeAvailability : vehicleTypesAvailable) {
        if (
          typeAvailability.getVehicleType() != null &&
          typeAvailability.getVehicleType().getFormFactor() != null &&
          availableFormFactors.contains(typeAvailability.getVehicleType().getFormFactor())
        ) {
          hasMatchingFormFactor = true;
          break;
        }
      }

      if (!hasMatchingFormFactor) {
        return false;
      }
    }

    // Filter by available propulsion types
    List<PropulsionType> availablePropulsionTypes =
      filterParameters.getAvailablePropulsionTypes();
    if (availablePropulsionTypes != null && !availablePropulsionTypes.isEmpty()) {
      // Check if station has vehicle types available with the requested propulsion types
      List<VehicleTypeAvailability> vehicleTypesAvailable =
        station.getVehicleTypesAvailable();

      // If no vehicle types are available, we can't filter by propulsion type
      if (vehicleTypesAvailable == null || vehicleTypesAvailable.isEmpty()) {
        return false;
      }

      boolean hasMatchingPropulsionType = false;
      for (VehicleTypeAvailability typeAvailability : vehicleTypesAvailable) {
        if (
          typeAvailability.getVehicleType() != null &&
          typeAvailability.getVehicleType().getPropulsionType() != null &&
          availablePropulsionTypes.contains(
            typeAvailability.getVehicleType().getPropulsionType()
          )
        ) {
          hasMatchingPropulsionType = true;
          break;
        }
      }

      return hasMatchingPropulsionType;
    }

    return true;
  }
}
