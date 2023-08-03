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

package org.entur.lamassu.mapper.feedmapper;

import java.util.List;
import java.util.stream.Collectors;
import org.entur.gbfs.v2_3.geofencing_zones.GBFSData;
import org.entur.gbfs.v2_3.geofencing_zones.GBFSFeature;
import org.entur.gbfs.v2_3.geofencing_zones.GBFSGeofencingZones;
import org.entur.gbfs.v2_3.geofencing_zones.GBFSGeofencingZones__1;
import org.entur.gbfs.v2_3.geofencing_zones.GBFSProperties;
import org.entur.gbfs.v2_3.geofencing_zones.GBFSRule;
import org.entur.lamassu.model.provider.FeedProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GeofencingZonesFeedMapper extends AbstractFeedMapper<GBFSGeofencingZones> {

  @Value("${org.entur.lamassu.targetGbfsVersion:2.2}")
  private String targetGbfsVersion;

  @Override
  public GBFSGeofencingZones map(GBFSGeofencingZones source, FeedProvider feedProvider) {
    if (source == null) {
      return null;
    }

    var mapped = new GBFSGeofencingZones();
    mapped.setVersion(targetGbfsVersion);
    mapped.setLastUpdated(source.getLastUpdated());
    mapped.setTtl(source.getTtl());
    mapped.setData(mapData(source.getData(), feedProvider));
    return mapped;
  }

  private GBFSData mapData(GBFSData data, FeedProvider feedProvider) {
    var mapped = new GBFSData();
    mapped.setGeofencingZones(
      mapGeofencingZones(data.getGeofencingZones(), feedProvider)
    );
    return mapped;
  }

  private GBFSGeofencingZones__1 mapGeofencingZones(
    GBFSGeofencingZones__1 geofencingZones,
    FeedProvider feedProvider
  ) {
    var mapped = new GBFSGeofencingZones__1();
    mapped.setType(geofencingZones.getType());
    mapped.setFeatures(mapFeatures(geofencingZones.getFeatures(), feedProvider));
    return mapped;
  }

  private List<GBFSFeature> mapFeatures(
    List<GBFSFeature> features,
    FeedProvider feedProvider
  ) {
    return features
      .stream()
      .map(feature -> mapFeature(feature, feedProvider))
      .collect(Collectors.toList());
  }

  private GBFSFeature mapFeature(GBFSFeature feature, FeedProvider feedProvider) {
    var mapped = new GBFSFeature();
    mapped.setType(feature.getType());
    mapped.setGeometry(feature.getGeometry());
    mapped.setProperties(mapProperties(feature.getProperties(), feedProvider));
    return mapped;
  }

  private GBFSProperties mapProperties(
    GBFSProperties properties,
    FeedProvider feedProvider
  ) {
    var mapped = new GBFSProperties();
    mapped.setName(properties.getName());
    mapped.setStart(properties.getStart());
    mapped.setEnd(properties.getEnd());
    mapped.setRules(
      properties
        .getRules()
        .stream()
        .map(rule -> mapRule(rule, feedProvider))
        .collect(Collectors.toList())
    );
    return mapped;
  }

  private GBFSRule mapRule(GBFSRule rule, FeedProvider feedProvider) {
    var mapped = new GBFSRule();
    mapped.setVehicleTypeId(
      IdMappers
        .mapIds(
          feedProvider.getCodespace(),
          IdMappers.VEHICLE_TYPE_ID_TYPE,
          rule.getVehicleTypeId()
        )
        .orElse(null)
    );
    mapped.setRideAllowed(rule.getRideAllowed());
    mapped.setMaximumSpeedKph(rule.getMaximumSpeedKph());
    mapped.setRideThroughAllowed(rule.getRideThroughAllowed());
    mapped.setStationParking(rule.getStationParking());
    return mapped;
  }
}
