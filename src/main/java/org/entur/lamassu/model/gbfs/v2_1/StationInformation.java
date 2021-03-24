package org.entur.lamassu.model.gbfs.v2_1;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public class StationInformation extends GBFSBase {
    @JsonProperty("data") Data data;

    public Data getData() {
        return data;
    }

    public static class Data {
        @JsonProperty("stations") List<Station> stations;

        public List<Station> getStations() {
            return stations;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
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

        public String getStationId() {
            return stationId;
        }

        public void setStationId(String stationId) {
            this.stationId = stationId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getShortName() {
            return shortName;
        }

        public void setShortName(String shortName) {
            this.shortName = shortName;
        }

        public Double getLat() {
            return lat;
        }

        public void setLat(Double lat) {
            this.lat = lat;
        }

        public Double getLon() {
            return lon;
        }

        public void setLon(Double lon) {
            this.lon = lon;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getCrossStreet() {
            return crossStreet;
        }

        public void setCrossStreet(String crossStreet) {
            this.crossStreet = crossStreet;
        }

        public String getRegionId() {
            return regionId;
        }

        public void setRegionId(String regionId) {
            this.regionId = regionId;
        }

        public String getPostCode() {
            return postCode;
        }

        public void setPostCode(String postCode) {
            this.postCode = postCode;
        }

        public List<RentalMethod> getRentalMethods() {
            return rentalMethods;
        }

        public void setRentalMethods(List<RentalMethod> rentalMethods) {
            this.rentalMethods = rentalMethods;
        }

        public Boolean getVirtualStation() {
            return isVirtualStation;
        }

        public void setVirtualStation(Boolean virtualStation) {
            isVirtualStation = virtualStation;
        }

        public MultiPolygon getStationArea() {
            return stationArea;
        }

        public void setStationArea(MultiPolygon stationArea) {
            this.stationArea = stationArea;
        }

        public Integer getCapacity() {
            return capacity;
        }

        public void setCapacity(Integer capacity) {
            this.capacity = capacity;
        }

        public Map<String, Integer> getVehicleCapacity() {
            return vehicleCapacity;
        }

        public void setVehicleCapacity(Map<String, Integer> vehicleCapacity) {
            this.vehicleCapacity = vehicleCapacity;
        }

        public Boolean getValetStation() {
            return isValetStation;
        }

        public void setValetStation(Boolean valetStation) {
            isValetStation = valetStation;
        }

        public RentalUris getRentalUris() {
            return rentalUris;
        }

        public void setRentalUris(RentalUris rentalUris) {
            this.rentalUris = rentalUris;
        }

        public Map<String, Integer> getVehicleTypeCapacity() {
            return vehicleTypeCapacity;
        }

        public void setVehicleTypeCapacity(Map<String, Integer> vehicleTypeCapacity) {
            this.vehicleTypeCapacity = vehicleTypeCapacity;
        }
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
