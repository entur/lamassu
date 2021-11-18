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

import java.io.Serializable;
import java.util.List;

public class GeofencingZones implements Entity {
    private String systemId;
    private FeatureCollection geojson;

    public String getSystemId() {
        return systemId;
    }

    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }

    public FeatureCollection getGeojson() {
        return geojson;
    }

    public void setGeojson(FeatureCollection geojson) {
        this.geojson = geojson;
    }

    @Override
    public String getId() {
        return getSystemId();
    }

    public static class FeatureCollection implements Serializable {
        private String type = "FeatureCollection";
        private List<Feature> features;

        public String getType() {
            return type;
        }

        public List<Feature> getFeatures() {
            return features;
        }

        public void setFeatures(List<Feature> features) {
            this.features = features;
        }
    }

    public static class Feature implements Serializable {
        private String type = "Feature";
        private MultiPolygon geometry;
        private Properties properties;

        public String getType() {
            return type;
        }

        public MultiPolygon getGeometry() {
            return geometry;
        }

        public void setGeometry(MultiPolygon geometry) {
            this.geometry = geometry;
        }

        public Properties getProperties() {
            return properties;
        }

        public void setProperties(Properties properties) {
            this.properties = properties;
        }
    }

    public static class Properties implements Serializable {
        private String name;
        private Long start;
        private Long end;
        private List<Rule> rules;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Long getStart() {
            return start;
        }

        public void setStart(Long start) {
            this.start = start;
        }

        public Long getEnd() {
            return end;
        }

        public void setEnd(Long end) {
            this.end = end;
        }

        public List<Rule> getRules() {
            return rules;
        }

        public void setRules(List<Rule> rules) {
            this.rules = rules;
        }
    }

    public static class Rule implements Serializable {
        private List<String> vehicleTypeIds;
        private Boolean rideAllowed;
        private Boolean rideThroughAllowed;
        private Integer maximumSpeedKph;

        public List<String> getVehicleTypeIds() {
            return vehicleTypeIds;
        }

        public void setVehicleTypeIds(List<String> vehicleTypeIds) {
            this.vehicleTypeIds = vehicleTypeIds;
        }

        public Boolean getRideAllowed() {
            return rideAllowed;
        }

        public void setRideAllowed(Boolean rideAllowed) {
            this.rideAllowed = rideAllowed;
        }

        public Boolean getRideThroughAllowed() {
            return rideThroughAllowed;
        }

        public void setRideThroughAllowed(Boolean rideThroughAllowed) {
            this.rideThroughAllowed = rideThroughAllowed;
        }

        public Integer getMaximumSpeedKph() {
            return maximumSpeedKph;
        }

        public void setMaximumSpeedKph(Integer maximumSpeedKph) {
            this.maximumSpeedKph = maximumSpeedKph;
        }
    }
}
