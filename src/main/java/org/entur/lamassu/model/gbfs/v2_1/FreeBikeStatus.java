package org.entur.lamassu.model.gbfs.v2_1;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class FreeBikeStatus extends GBFSBase {
    @JsonProperty("data") Data data;

    public Data getData() {
        return data;
    }

    public static class Data {
        @JsonProperty("bikes")
        List<Bike> bikes;

        public List<Bike> getBikes() {
            return bikes;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Bike {
        @JsonProperty("bike_id") String bikeId;
        @JsonProperty("last_reported") Long lastReported;
        @JsonProperty("lat") Double lat;
        @JsonProperty("lon") Double lon;
        @JsonProperty("is_reserved") Boolean isReserved;
        @JsonProperty("is_disabled") Boolean isDisabled;
        @JsonProperty("vehicle_type_id") String vehicleTypeId;
        @JsonProperty("rental_uris") RentalUris rentalUris;
        @JsonProperty("current_range_meters") Float currentRangeMeters;
        @JsonProperty("station_id") String stationId;
        @JsonProperty("pricing_plan_id") String pricingPlanId;

        public String getBikeId() {
            return bikeId;
        }

        public Double getLat() {
            return lat;
        }

        public Double getLon() {
            return lon;
        }

        @Override
        public String toString() {
            return "Bike{" +
                    "bikeId='" + bikeId + '\'' +
                    ", lastReported=" + lastReported +
                    ", lat=" + lat +
                    ", lon=" + lon +
                    ", isReserved=" + isReserved +
                    ", isDisabled=" + isDisabled +
                    ", vehicleTypeId='" + vehicleTypeId + '\'' +
                    ", rentalUris=" + rentalUris +
                    ", currentRangeMeters=" + currentRangeMeters +
                    ", stationId='" + stationId + '\'' +
                    ", pricingPlanId='" + pricingPlanId + '\'' +
                    '}';
        }
    }
}
