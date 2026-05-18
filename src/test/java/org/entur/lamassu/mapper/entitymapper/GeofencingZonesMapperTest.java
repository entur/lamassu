package org.entur.lamassu.mapper.entitymapper;

import java.util.Collections;
import org.entur.lamassu.model.entities.GeofencingZones;
import org.entur.lamassu.model.provider.FeedProvider;
import org.geojson.MultiPolygon;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mobilitydata.gbfs.v3_0.geofencing_zones.GBFSFeature;
import org.mobilitydata.gbfs.v3_0.geofencing_zones.GBFSProperties;

public class GeofencingZonesMapperTest {

  @Test
  void testMapFeedWhenGeofencingZoneHasNoRules() {
    var mapper = new GeofencingZonesMapper();
    var feature = new GBFSFeature();
    feature.setProperties(new GBFSProperties());
    feature.setGeometry(new MultiPolygon());
    var mappedFeature = mapper.mapFeature(feature, "en");
    Assertions.assertNull(mappedFeature.getProperties().getRules());
  }
}
