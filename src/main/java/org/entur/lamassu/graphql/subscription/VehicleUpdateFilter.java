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

package org.entur.lamassu.graphql.subscription;

import org.entur.lamassu.model.subscription.VehicleUpdate;
import org.entur.lamassu.service.BoundingBoxQueryParameters;
import org.entur.lamassu.service.RangeQueryParameters;
import org.entur.lamassu.service.VehicleFilterParameters;

public class VehicleUpdateFilter implements EntityUpdateFilter<VehicleUpdate> {

  private final VehicleFilterParameters filterParameters;
  private final BoundingBoxQueryParameters boundingBoxParameters;
  private final RangeQueryParameters rangeQueryParameters;

  public VehicleUpdateFilter(
    VehicleFilterParameters filterParameters,
    RangeQueryParameters rangeQueryParameters
  ) {
    this.filterParameters = filterParameters;
    this.rangeQueryParameters = rangeQueryParameters;
    this.boundingBoxParameters = null;
  }

  public VehicleUpdateFilter(
    VehicleFilterParameters filterParameters,
    BoundingBoxQueryParameters boundingBoxParameters
  ) {
    this.filterParameters = filterParameters;
    this.boundingBoxParameters = boundingBoxParameters;
    this.rangeQueryParameters = null;
  }

  @Override
  public boolean matches(VehicleUpdate entity) {
    return true;
  }

  public VehicleFilterParameters getFilterParameters() {
    return filterParameters;
  }

  public BoundingBoxQueryParameters getBoundingBoxParameters() {
    return boundingBoxParameters;
  }

  public RangeQueryParameters getRangeQueryParameters() {
    return rangeQueryParameters;
  }
}
