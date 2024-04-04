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

package org.entur.lamassu.model.entities;

import com.mapbox.geojson.Point;
import com.mapbox.geojson.utils.PolylineUtils;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

public class MultiPolygon implements Serializable {

  private String type = "MultiPolygon";
  private List<List<List<List<Double>>>> coordinates;

  public String getType() {
    return type;
  }

  public List<List<List<List<Double>>>> getCoordinates() {
    return coordinates;
  }

  public void setCoordinates(List<List<List<List<Double>>>> coordinates) {
    this.coordinates = coordinates;
  }

  public static MultiPolygon fromGeoJson(org.geojson.MultiPolygon geometry) {
    var mapped = new MultiPolygon();
    var coordinates = geometry.getCoordinates();
    var mappedCoordinates = coordinates
      .stream()
      .map(e ->
        e
          .stream()
          .map(f ->
            f
              .stream()
              .map(lngLatAlt -> List.of(lngLatAlt.getLongitude(), lngLatAlt.getLatitude())
              )
              .collect(Collectors.toList())
          )
          .collect(Collectors.toList())
      )
      .collect(Collectors.toList());
    mapped.setCoordinates(mappedCoordinates);

    return mapped;
  }
}
