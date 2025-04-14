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

import static org.entur.lamassu.util.GeoUtils.calculateDistance;

import java.util.function.Function;
import java.util.function.UnaryOperator;
import org.entur.lamassu.model.entities.LocationEntity;
import org.entur.lamassu.model.entities.SystemEntity;
import org.entur.lamassu.service.BoundingBoxQueryParameters;
import org.entur.lamassu.service.FilterParameters;
import org.entur.lamassu.service.RangeQueryParameters;

public class AbstractEntityUpdateFilter<
  E extends LocationEntity, F extends FilterParameters
> {

  private final F filterParameters;
  private final BoundingBoxQueryParameters boundingBoxParameters;
  private final RangeQueryParameters rangeQueryParameters;
  private final Function<String, String> codespaceResolver;

  protected AbstractEntityUpdateFilter(
    F filterParameters,
    BoundingBoxQueryParameters boundingBoxParameters,
    UnaryOperator<String> codespaceResolver
  ) {
    this.filterParameters = filterParameters;
    this.boundingBoxParameters = boundingBoxParameters;
    this.rangeQueryParameters = null;
    this.codespaceResolver = codespaceResolver;
  }

  protected AbstractEntityUpdateFilter(
    F filterParameters,
    RangeQueryParameters rangeQueryParameters,
    UnaryOperator<String> codespaceResolver
  ) {
    this.filterParameters = filterParameters;
    this.rangeQueryParameters = rangeQueryParameters;
    this.boundingBoxParameters = null;
    this.codespaceResolver = codespaceResolver;
  }

  public F getFilterParameters() {
    return filterParameters;
  }

  public BoundingBoxQueryParameters getBoundingBoxParameters() {
    return boundingBoxParameters;
  }

  public RangeQueryParameters getRangeQueryParameters() {
    return rangeQueryParameters;
  }

  protected boolean doesNotMatchSpatialFilters(E entity) {
    // Skip spatial filtering if coordinates are not available
    if (entity.getLat() == null || entity.getLon() == null) {
      return true;
    }

    // Check bounding box filter
    if (boundingBoxParameters != null) {
      double lat = entity.getLat();
      double lon = entity.getLon();

      if (
        lat < boundingBoxParameters.getMinimumLatitude() ||
        lat > boundingBoxParameters.getMaximumLatitude() ||
        lon < boundingBoxParameters.getMinimumLongitude() ||
        lon > boundingBoxParameters.getMaximumLongitude()
      ) {
        return true;
      }
    }

    // Check range filter
    if (rangeQueryParameters != null) {
      double stationLat = entity.getLat();
      double stationLon = entity.getLon();
      double centerLat = rangeQueryParameters.getLat();
      double centerLon = rangeQueryParameters.getLon();
      double range = rangeQueryParameters.getRange();

      double distance = calculateDistance(centerLat, centerLon, stationLat, stationLon);

      return distance > range;
    }

    return false;
  }

  protected boolean doesNotMatchBasicFilters(SystemEntity entity) {
    // Skip filtering if no filter parameters are set
    if (filterParameters == null) {
      return false;
    }

    // Filter by codespace
    if (
      filterParameters.getCodespaces() != null &&
      !filterParameters.getCodespaces().isEmpty()
    ) {
      String entityCodespace = codespaceResolver.apply(entity.getSystemId());
      if (!filterParameters.getCodespaces().contains(entityCodespace)) {
        return true;
      }
    }

    // Filter by system
    if (
      filterParameters.getSystems() != null && !filterParameters.getSystems().isEmpty()
    ) {
      String systemId = entity.getSystemId();
      if (!filterParameters.getSystems().contains(systemId)) {
        return true;
      }
    }

    // Filter by operator
    if (
      filterParameters.getOperators() != null &&
      !filterParameters.getOperators().isEmpty()
    ) {
      String operatorId = null;
      if (entity.getSystem() != null && entity.getSystem().getOperator() != null) {
        operatorId = entity.getSystem().getOperator().getId();
      }

      return operatorId == null || !filterParameters.getOperators().contains(operatorId);
    }

    return false;
  }
}
