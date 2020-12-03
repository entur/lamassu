package org.entur.lamassu.model.gbfs.v2_1;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public class StationInformation extends GBFSBase {
    @JsonProperty("data") Data data;

    public static class Data {
        @JsonProperty("stations") List<Station> stations;
    }

    public static class Station {
        @JsonProperty("station_id") String stationId;
        @JsonProperty("name") String name;
        @JsonProperty("short_name") String shortName;
        @JsonProperty("lat") Double lat;
        @JsonProperty("lon") Double lon;
        @JsonProperty("address") String address;
        @JsonProperty("cross_street") String crossStreet;
        @JsonProperty("region_id") String regionId;
        @JsonProperty("post_code") String postCode;
        @JsonProperty("rental_methods") List<RentalMethod> rentalMethods;
        @JsonProperty("is_virtual_station") Boolean isVirtualStation;
        @JsonProperty("station_area") MultiPolygon stationArea;
        @JsonProperty("capacity") Integer capacity;
        @JsonProperty("vehicle_capacity")  Map<String, Integer> vehicleCapacity;
        @JsonProperty("is_valet_station") Boolean isValetStation;
        @JsonProperty("rental_uris") RentalUris rentalUris;
        @JsonProperty("vehicle_type_capacity") Map<String, Integer> vehicleTypeCapacity;
    }

    public enum RentalMethod {
        KEY,
        CREDITCARD,
        PAYPASS,
        APPLEPAY,
        ANDROIDPAY,
        TRANSITCARD,
        ACCOUNTNUMBER,
        PHONE
    }
}
