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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.entur.lamassu.model.entities.GeofencingZones;
import org.entur.lamassu.model.entities.GeofencingZonesData;
import org.entur.lamassu.model.entities.MultiPolygon;
import org.entur.lamassu.model.provider.FeedProvider;
import org.mobilitydata.gbfs.v3_0.geofencing_zones.GBFSData;
import org.mobilitydata.gbfs.v3_0.geofencing_zones.GBFSFeature;
import org.mobilitydata.gbfs.v3_0.geofencing_zones.GBFSGeofencingZones;
import org.mobilitydata.gbfs.v3_0.geofencing_zones.GBFSGlobalRule;
import org.mobilitydata.gbfs.v3_0.geofencing_zones.GBFSName;
import org.mobilitydata.gbfs.v3_0.geofencing_zones.GBFSProperties;
import org.mobilitydata.gbfs.v3_0.geofencing_zones.GBFSRule;
import org.springframework.stereotype.Component;

@Component
public class GeofencingZonesDataMapper {

  public GeofencingZonesData map(
    GBFSGeofencingZones geofencingZonesResponse,
    FeedProvider feedProvider
  ) {
    if (geofencingZonesResponse == null || geofencingZonesResponse.getData() == null) {
      return null;
    }

    var mapped = new GeofencingZonesData();
    mapped.setSystemId(feedProvider.getSystemId());

    GBFSData data = geofencingZonesResponse.getData();

    // Map zones with optimized structure
    mapped.setZones(mapZones(data, feedProvider.getLanguage()));

    // Map global rules
    mapped.setGlobalRules(mapGlobalRules(data.getGlobalRules()));

    return mapped;
  }

  private List<GeofencingZonesData.GeofencingZone> mapZones(
    GBFSData data,
    String language
  ) {
    if (
      data.getGeofencingZones() == null || data.getGeofencingZones().getFeatures() == null
    ) {
      return Collections.emptyList();
    }

    return data
      .getGeofencingZones()
      .getFeatures()
      .stream()
      .map(feature -> mapZone(feature, language))
      .toList();
  }

  private GeofencingZonesData.GeofencingZone mapZone(
    GBFSFeature feature,
    String language
  ) {
    var zone = new GeofencingZonesData.GeofencingZone();

    // Map properties
    if (feature.getProperties() != null) {
      GBFSProperties properties = feature.getProperties();

      // Map name
      zone.setName(extractName(properties.getName(), language));

      // Map time bounds
      zone.setStart(
        properties.getStart() != null ? properties.getStart().getTime() / 1000 : null
      );
      zone.setEnd(
        properties.getEnd() != null ? properties.getEnd().getTime() / 1000 : null
      );

      // Map rules
      zone.setRules(mapRules(properties.getRules()));
    }

    // Map and encode geometry as polyline
    zone.setPolylineEncodedMultiPolygon(
      encodeMultiPolygon(MultiPolygon.fromGeoJson(feature.getGeometry()))
    );

    return zone;
  }

  private String extractName(List<GBFSName> names, String language) {
    if (names == null || names.isEmpty()) {
      return null;
    }

    return names
      .stream()
      .filter(name -> name.getLanguage().equals(language))
      .map(GBFSName::getText)
      .findFirst()
      .orElse(
        // Fallback to first available name if language not found
        names.get(0).getText()
      );
  }

  private List<List<String>> encodeMultiPolygon(MultiPolygon multiPolygon) {
    if (multiPolygon == null || multiPolygon.getCoordinates() == null) {
      return Collections.emptyList();
    }

    return multiPolygon
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
  }

  private List<GeofencingZones.Rule> mapRules(List<GBFSRule> rules) {
    if (rules == null) {
      return Collections.emptyList();
    }
    return rules.stream().map(this::mapRule).toList();
  }

  private List<GeofencingZones.Rule> mapGlobalRules(List<GBFSGlobalRule> globalRules) {
    if (globalRules == null) {
      return Collections.emptyList();
    }
    return globalRules.stream().map(this::mapGlobalRule).toList();
  }

  private GeofencingZones.Rule mapRule(GBFSRule rule) {
    var mapped = new GeofencingZones.Rule();
    mapped.setVehicleTypeIds(rule.getVehicleTypeIds());
    mapped.setRideAllowed(rule.getRideStartAllowed() && rule.getRideEndAllowed());
    mapped.setRideStartAllowed(rule.getRideStartAllowed());
    mapped.setRideEndAllowed(rule.getRideEndAllowed());
    mapped.setRideThroughAllowed(rule.getRideThroughAllowed());
    mapped.setMaximumSpeedKph(
      rule.getMaximumSpeedKph() != null ? rule.getMaximumSpeedKph() : null
    );
    mapped.setStationParking(rule.getStationParking());
    return mapped;
  }

  private GeofencingZones.Rule mapGlobalRule(GBFSGlobalRule rule) {
    var mapped = new GeofencingZones.Rule();
    mapped.setVehicleTypeIds(rule.getVehicleTypeIds());
    mapped.setRideAllowed(rule.getRideStartAllowed() && rule.getRideEndAllowed());
    mapped.setRideStartAllowed(rule.getRideStartAllowed());
    mapped.setRideEndAllowed(rule.getRideEndAllowed());
    mapped.setRideThroughAllowed(rule.getRideThroughAllowed());
    mapped.setMaximumSpeedKph(
      rule.getMaximumSpeedKph() != null ? rule.getMaximumSpeedKph() : null
    );
    mapped.setStationParking(rule.getStationParking());
    return mapped;
  }
}
