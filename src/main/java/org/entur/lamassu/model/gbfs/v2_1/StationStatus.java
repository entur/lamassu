package org.entur.lamassu.model.gbfs.v2_1;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class StationStatus extends GBFSBase {
    @JsonProperty("data") Data data;

    public Data getData() {
        return data;
    }

    public static class Data {
        @JsonProperty("stations")
        List<Station> stations;

        public List<Station> getStations() {
            return stations;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
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

        public String getStationId() {
            return stationId;
        }

        public void setStationId(String stationId) {
            this.stationId = stationId;
        }

        public Integer getNumBikesAvailable() {
            return numBikesAvailable;
        }

        public void setNumBikesAvailable(Integer numBikesAvailable) {
            this.numBikesAvailable = numBikesAvailable;
        }

        public List<VehicleTypeAvailability> getVehicleTypesAvailable() {
            return vehicleTypesAvailable;
        }

        public void setVehicleTypesAvailable(List<VehicleTypeAvailability> vehicleTypesAvailable) {
            this.vehicleTypesAvailable = vehicleTypesAvailable;
        }

        public Integer getNumBikesDisabled() {
            return numBikesDisabled;
        }

        public void setNumBikesDisabled(Integer numBikesDisabled) {
            this.numBikesDisabled = numBikesDisabled;
        }

        public Integer getNumDocksAvailable() {
            return numDocksAvailable;
        }

        public void setNumDocksAvailable(Integer numDocksAvailable) {
            this.numDocksAvailable = numDocksAvailable;
        }

        public List<VehicleDockAvailability> getVehicleDocksAvailable() {
            return vehicleDocksAvailable;
        }

        public void setVehicleDocksAvailable(List<VehicleDockAvailability> vehicleDocksAvailable) {
            this.vehicleDocksAvailable = vehicleDocksAvailable;
        }

        public Integer getNumDocksDisabled() {
            return numDocksDisabled;
        }

        public void setNumDocksDisabled(Integer numDocksDisabled) {
            this.numDocksDisabled = numDocksDisabled;
        }

        public Boolean getInstalled() {
            return isInstalled;
        }

        public void setInstalled(Boolean installed) {
            isInstalled = installed;
        }

        public Boolean getRenting() {
            return isRenting;
        }

        public void setRenting(Boolean renting) {
            isRenting = renting;
        }

        public Boolean getReturning() {
            return isReturning;
        }

        public void setReturning(Boolean returning) {
            isReturning = returning;
        }

        public Long getLastReported() {
            return lastReported;
        }

        public void setLastReported(Long lastReported) {
            this.lastReported = lastReported;
        }
    }

    public static class VehicleTypeAvailability {
        @JsonProperty("vehicle_type_id") String vehicleTypeId;
        @JsonProperty("count") Integer count;

        public String getVehicleTypeId() {
            return vehicleTypeId;
        }

        public void setVehicleTypeId(String vehicleTypeId) {
            this.vehicleTypeId = vehicleTypeId;
        }

        public Integer getCount() {
            return count;
        }

        public void setCount(Integer count) {
            this.count = count;
        }
    }

    public static class VehicleDockAvailability {
        @JsonProperty("vehicle_type_ids") List<String> vehicleTypeIds;
        @JsonProperty("count") Integer count;

        public List<String> getVehicleTypeIds() {
            return vehicleTypeIds;
        }

        public void setVehicleTypeIds(List<String> vehicleTypeIds) {
            this.vehicleTypeIds = vehicleTypeIds;
        }

        public Integer getCount() {
            return count;
        }

        public void setCount(Integer count) {
            this.count = count;
        }
    }
}
