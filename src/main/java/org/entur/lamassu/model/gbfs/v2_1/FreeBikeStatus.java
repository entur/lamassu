package org.entur.lamassu.model.gbfs.v2_1;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class FreeBikeStatus extends GBFSBase {
    @JsonProperty("data") Data data;

    public Data getData() {
        return data;
    }

    @Override
    public String toString() {
        return "FreeBikeStatus{" +
                "data=" + data +
                ", lastUpdated=" + lastUpdated +
                ", ttl=" + ttl +
                ", version='" + version + '\'' +
                '}';
    }

    public static class Data {
        @JsonProperty("bikes")
        List<Bike> bikes;

        public List<Bike> getBikes() {
            return bikes;
        }

        @Override
        public String toString() {
            return "Data{" +
                    "bikes=" + bikes +
                    '}';
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

        @JsonProperty("is_reserved")
        public Boolean getReserved() {
            return isReserved;
        }

        @JsonProperty("is_disabled")
        public Boolean getDisabled() {
            return isDisabled;
        }

        public RentalUris getRentalUris() {
            return rentalUris;
        }

        public String getVehicleTypeId() {
            return vehicleTypeId;
        }

        public String getStationId() {
            return stationId;
        }

        public String getPricingPlanId() {
            return pricingPlanId;
        }

        public Float getCurrentRangeMeters() {
            return currentRangeMeters;
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
