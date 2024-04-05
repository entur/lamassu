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

package org.entur.lamassu.mapper.entitymapper;

import com.mapbox.geojson.Point;
import com.mapbox.geojson.utils.PolylineUtils;
import java.util.List;
import java.util.stream.Collectors;
import org.entur.gbfs.v2_3.geofencing_zones.GBFSFeature;
import org.entur.gbfs.v2_3.geofencing_zones.GBFSGeofencingZones__1;
import org.entur.gbfs.v2_3.geofencing_zones.GBFSProperties;
import org.entur.gbfs.v2_3.geofencing_zones.GBFSRule;
import org.entur.lamassu.model.entities.GeofencingZones;
import org.entur.lamassu.model.entities.MultiPolygon;
import org.entur.lamassu.model.provider.FeedProvider;
import org.springframework.stereotype.Component;

@Component
public class GeofencingZonesMapper {

  public org.entur.lamassu.model.entities.GeofencingZones map(
    GBFSGeofencingZones__1 geofencingZones,
    FeedProvider feedProvider
  ) {
    var mapped = new org.entur.lamassu.model.entities.GeofencingZones();
    mapped.setSystemId(feedProvider.getSystemId());
    mapped.setGeojson(mapGeojson(geofencingZones));

    addPolylineEncodedMultiPolygon(mapped);

    return mapped;
  }

  private void addPolylineEncodedMultiPolygon(GeofencingZones geofencingZones) {
    for (GeofencingZones.Feature feature : geofencingZones.getGeojson().getFeatures()) {
      var mappedPolylineEncodedMultiPolygon = feature
        .getGeometry()
        .getCoordinates()
        .stream()
        .map(polygon ->
          polygon
            .stream()
            .map(ring ->
              ring
                .stream()
                .map(coords -> Point.fromLngLat(coords.get(0), coords.get(1)))
                      .toList()
            )
            .map(ring -> PolylineUtils.encode(ring, 6))
                  .toList()
        )
        .toList();

      feature
        .getProperties()
        .setPolylineEncodedMultiPolygon(mappedPolylineEncodedMultiPolygon);
    }
  }

  private org.entur.lamassu.model.entities.GeofencingZones.FeatureCollection mapGeojson(
    GBFSGeofencingZones__1 geofencingZones
  ) {
    var mapped = new org.entur.lamassu.model.entities.GeofencingZones.FeatureCollection();
    mapped.setFeatures(mapFeatures(geofencingZones.getFeatures()));
    return mapped;
  }

  private List<org.entur.lamassu.model.entities.GeofencingZones.Feature> mapFeatures(
    List<GBFSFeature> features
  ) {
    return features.stream().map(this::mapFeature).collect(Collectors.toList());
  }

  private org.entur.lamassu.model.entities.GeofencingZones.Feature mapFeature(
    GBFSFeature feature
  ) {
    var mapped = new org.entur.lamassu.model.entities.GeofencingZones.Feature();
    mapped.setProperties(mapProperties(feature.getProperties()));
    mapped.setGeometry(MultiPolygon.fromGeoJson(feature.getGeometry()));
    return mapped;
  }

  private org.entur.lamassu.model.entities.GeofencingZones.Properties mapProperties(
    GBFSProperties properties
  ) {
    var mapped = new org.entur.lamassu.model.entities.GeofencingZones.Properties();
    mapped.setName(properties.getName());
    mapped.setStart(
      properties.getStart() != null ? properties.getStart().longValue() : null
    );
    mapped.setEnd(properties.getEnd() != null ? properties.getEnd().longValue() : null);
    mapped.setRules(mapRules(properties.getRules()));
    return mapped;
  }

  private List<org.entur.lamassu.model.entities.GeofencingZones.Rule> mapRules(
    List<GBFSRule> rules
  ) {
    return rules.stream().map(this::mapRule).collect(Collectors.toList());
  }

  private org.entur.lamassu.model.entities.GeofencingZones.Rule mapRule(GBFSRule rule) {
    var mapped = new org.entur.lamassu.model.entities.GeofencingZones.Rule();
    mapped.setVehicleTypeIds(rule.getVehicleTypeId());
    mapped.setRideAllowed(rule.getRideAllowed());
    mapped.setRideThroughAllowed(rule.getRideThroughAllowed());
    mapped.setMaximumSpeedKph(
      rule.getMaximumSpeedKph() != null ? rule.getMaximumSpeedKph().intValue() : null
    );
    mapped.setStationParking(rule.getStationParking());
    return mapped;
  }
}
