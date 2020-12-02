package org.entur.lamassu.model.gbfs.v2_1;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.geojson.MultiPolygon;

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

        public MultiPolygon getGeometry() {
            return geometry;
        }
    }

    public static class Properties {
        @JsonProperty("name") String name;
        @JsonProperty("start") Long start;
        @JsonProperty("end") Long end;
        @JsonProperty("rules") List<Rule> rules;
    }

    public static class Rule {
        @JsonProperty("vehicle_type_ids") List<String> vehicleTypeIds;
        @JsonProperty("ride_allowed") Boolean rideAllowed;
        @JsonProperty("ride_through_allowed") Boolean rideThroughAllowed;
        @JsonProperty("maximum_speed_kph") Integer maximumSpeedKph;
    }
}
