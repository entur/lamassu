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

package org.entur.lamassu.mapper;

import org.entur.lamassu.model.discovery.FeedProvider;
import org.entur.lamassu.model.gbfs.v2_1.GeofencingZones;
import org.entur.lamassu.model.gbfs.v2_1.MultiPolygon;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class GeofencingZonesMapper {
    public org.entur.lamassu.model.entities.GeofencingZones map(GeofencingZones.FeatureCollection geofencingZones, FeedProvider feedProvider) {
        var mapped = new org.entur.lamassu.model.entities.GeofencingZones();
        mapped.setSystemId(feedProvider.getSystemId());
        mapped.setGeojson(mapGeojson(geofencingZones));
        return mapped;
    }

    private org.entur.lamassu.model.entities.GeofencingZones.FeatureCollection mapGeojson(GeofencingZones.FeatureCollection geofencingZones) {
        var mapped = new org.entur.lamassu.model.entities.GeofencingZones.FeatureCollection();
        mapped.setFeatures(mapFeatures(geofencingZones.getFeatures()));
        return mapped;
    }

    private List<org.entur.lamassu.model.entities.GeofencingZones.Feature> mapFeatures(List<GeofencingZones.Feature> features) {
        return features.stream()
                .map(this::mapFeature)
                .collect(Collectors.toList());
    }

    private org.entur.lamassu.model.entities.GeofencingZones.Feature mapFeature(GeofencingZones.Feature feature) {
        var mapped = new org.entur.lamassu.model.entities.GeofencingZones.Feature();
        mapped.setProperties(mapProperties(feature.getProperties()));
        mapped.setGeometry(mapGeometry(feature.getGeometry()));
        return mapped;
    }

    private org.entur.lamassu.model.entities.GeofencingZones.Properties mapProperties(GeofencingZones.Properties properties) {
        var mapped = new org.entur.lamassu.model.entities.GeofencingZones.Properties();
        mapped.setName(properties.getName());
        mapped.setStart(properties.getStart());
        mapped.setEnd(properties.getEnd());
        mapped.setRules(mapRules(properties.getRules()));
        return mapped;
    }

    private List<org.entur.lamassu.model.entities.GeofencingZones.Rule> mapRules(List<GeofencingZones.Rule> rules) {
        return rules.stream()
                .map(this::mapRule)
                .collect(Collectors.toList());
    }

    private org.entur.lamassu.model.entities.GeofencingZones.Rule mapRule(GeofencingZones.Rule rule) {
        var mapped = new org.entur.lamassu.model.entities.GeofencingZones.Rule();
        mapped.setVehicleTypeIds(rule.getVehicleTypeIds());
        mapped.setRideAllowed(rule.getRideAllowed());
        mapped.setRideThroughAllowed(rule.getRideThroughAllowed());
        mapped.setMaximumSpeedKph(rule.getMaximumSpeedKph());
        return mapped;
    }

    private org.entur.lamassu.model.entities.GeofencingZones.MultiPolygon mapGeometry(MultiPolygon geometry) {
        var mapped = new org.entur.lamassu.model.entities.GeofencingZones.MultiPolygon();
        mapped.setCoordinates(geometry.getCoordinates());
        return mapped;
    }
}
