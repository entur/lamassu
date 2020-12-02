package org.entur.lamassu.model.gbfs.v2_1;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class StationStatus extends GBFSBase {
    @JsonProperty("data") Data data;

    public static class Data {
        @JsonProperty("stations")
        List<Station> stations;
    }

    public static class Station {
        @JsonProperty("station_id") String stationId;
        @JsonProperty("num_bikes_available") Integer numBikesAvailable;
        @JsonProperty("vehicle_types_available") List<VehicleTypeAvailability> vehicleTypesAvailable;
        @JsonProperty("num_bikes_disabled") Integer numBikesDisabled;
        @JsonProperty("num_docks_available") Integer numDocksAvailable;
        @JsonProperty("vehicle_docks_available") List<VehicleDockAvailability> vehicleDocksAvailable;
        @JsonProperty("num_docks_disabled") Integer numDocksDisabled;
        @JsonProperty("is_installed") Boolean isInstalled;
        @JsonProperty("is_renting") Boolean isRenting;
        @JsonProperty("is_returning") Boolean isReturning;
        @JsonProperty("last_reported") Long lastReported;
    }

    public static class VehicleTypeAvailability {
        @JsonProperty("vehicle_type_id") String vehicleTypeId;
        @JsonProperty("count") Integer count;
    }

    public static class VehicleDockAvailability {
        @JsonProperty("vehicle_type_ids") List<String> vehicleTypeIds;
        @JsonProperty("count") Integer count;
    }
}
