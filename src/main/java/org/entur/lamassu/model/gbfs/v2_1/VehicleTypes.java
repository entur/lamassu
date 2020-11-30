package org.entur.lamassu.model.gbfs.v2_1;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class VehicleTypes extends GBFSBase {
    @JsonProperty("data") Data data;

    public static class Data {
        @JsonProperty("vehicle_types")
        List<VehicleType> vehicleTypes;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class VehicleType {
        @JsonProperty("vehicle_type_id") String vehicleTypeId;
        @JsonProperty("form_factor") FormFactor formFactor;
        @JsonProperty("propulsion_type") PropulsionType propulsionType;
        @JsonProperty("max_range_meters") Float maxRangeMeters;
        @JsonProperty("name") String name;
    }

    public enum FormFactor {
        @JsonProperty("bicycle") BICYCLE,
        @JsonProperty("car") CAR,
        @JsonProperty("moped") MOPED,
        @JsonProperty("scooter") SCOOTER,
        @JsonProperty("other") OTHER
    }

    public enum PropulsionType {
        @JsonProperty("human") HUMAN,
        @JsonProperty("electric_assist") ELECTRIC_ASSIST,
        @JsonProperty("electric") ELECTRIC,
        @JsonProperty("combustion") COMBUSTION
    }
}
