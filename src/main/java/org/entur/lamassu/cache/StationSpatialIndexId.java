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

package org.entur.lamassu.cache;

import java.util.List;
import java.util.Objects;
import org.entur.lamassu.model.entities.FormFactor;
import org.entur.lamassu.model.entities.PropulsionType;

public class StationSpatialIndexId
  extends AbstractSpatialIndexId
  implements SpatialIndexId {

  private List<FormFactor> availableFormFactors;
  private List<PropulsionType> availablePropulsionTypes;

  public List<FormFactor> getAvailableFormFactors() {
    return availableFormFactors;
  }

  public void setAvailableFormFactors(List<FormFactor> availableFormFactors) {
    this.availableFormFactors = availableFormFactors;
  }

  public List<PropulsionType> getAvailablePropulsionTypes() {
    return availablePropulsionTypes;
  }

  public void setAvailablePropulsionTypes(List<PropulsionType> availablePropulsionTypes) {
    this.availablePropulsionTypes = availablePropulsionTypes;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    StationSpatialIndexId that = (StationSpatialIndexId) o;

    if (!Objects.equals(availableFormFactors, that.availableFormFactors)) return false;
    return Objects.equals(availablePropulsionTypes, that.availablePropulsionTypes);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result =
      31 * result + (availableFormFactors != null ? availableFormFactors.hashCode() : 0);
    result =
      31 *
      result +
      (availablePropulsionTypes != null ? availablePropulsionTypes.hashCode() : 0);
    return result;
  }
}
