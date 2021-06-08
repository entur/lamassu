package org.entur.lamassu.model.gbfs.v2_1;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class GeofencingZones extends GBFSBase {
    @JsonProperty("data") Data data;

    public Data getData() {
        return data;
    }

    public static class Data {
        @JsonProperty("geofencing_zones") FeatureCollection geofencingZones;

        public FeatureCollection getGeofencingZones() {
            return geofencingZones;
        }
    }

    public static class FeatureCollection {
        @JsonProperty("type") String type = "FeatureCollection";
        @JsonProperty("features") List<Feature> features;

        public List<Feature> getFeatures() {
            return features;
        }
    }

    public static class Feature {
        @JsonProperty("type") String type = "Feature";
        @JsonProperty("geometry") MultiPolygon geometry;
        @JsonProperty("properties") Properties properties;

        public String getType() {
            return type;
        }

        public MultiPolygon getGeometry() {
            return geometry;
        }

        public Properties getProperties() {
            return properties;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Properties {
        @JsonProperty("name") String name;
        @JsonProperty("start") Long start;
        @JsonProperty("end") Long end;
        @JsonProperty("rules") List<Rule> rules;

        public String getName() {
            return name;
        }

        public Long getStart() {
            return start;
        }

        public Long getEnd() {
            return end;
        }

        public List<Rule> getRules() {
            return rules;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Rule {
        @JsonProperty("vehicle_type_ids") List<String> vehicleTypeIds;
        @JsonProperty("ride_allowed") Boolean rideAllowed;
        @JsonProperty("ride_through_allowed") Boolean rideThroughAllowed;
        @JsonProperty("maximum_speed_kph") Integer maximumSpeedKph;

        public List<String> getVehicleTypeIds() {
            return vehicleTypeIds;
        }

        public Boolean getRideAllowed() {
            return rideAllowed;
        }

        public Boolean getRideThroughAllowed() {
            return rideThroughAllowed;
        }

        public Integer getMaximumSpeedKph() {
            return maximumSpeedKph;
        }
    }
}
