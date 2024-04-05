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

public class PolylineEncodedMultiPolygon implements Serializable {

    private String type = "PolylineEncodedMultiPolygon";
    private List<List<String>> coordinates;

    public String getType() {
        return type;
    }

    public List<List<String>> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(List<List<String>> coordinates) {
        this.coordinates = coordinates;
    }

    public static PolylineEncodedMultiPolygon fromGeoJson(org.geojson.MultiPolygon geometry) {
        var mapped = new PolylineEncodedMultiPolygon();
        var coordinates = geometry.getCoordinates();
        var mappedPolylineEncodedMultiPolygon = coordinates
                .stream()
                .map(polygon ->
                        polygon
                                .stream()
                                .map(ring ->
                                        ring
                                                .stream()
                                                .map(lngLatAlt -> Point.fromLngLat(lngLatAlt.getLongitude(), lngLatAlt.getLatitude()))
                                                .collect(Collectors.toList())
                                )
                                .map(ring -> PolylineUtils.encode(ring, 5))
                                .collect(Collectors.toList())
                )
                .toList();
        mapped.setCoordinates(mappedPolylineEncodedMultiPolygon);

        return mapped;
    }
}
