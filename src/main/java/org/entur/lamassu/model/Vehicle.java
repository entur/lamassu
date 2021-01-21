package org.entur.lamassu.model;

public class Vehicle implements Entity {
    private String id;
    private Double lat;
    private Double lon;
    private Boolean isReserved;
    private Boolean isDisabled;
    private Float currentRangeMeters;
    private VehicleType vehicleType;
    private PricingPlan pricingPlan;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public Boolean getReserved() {
        return isReserved;
    }

    public void setReserved(Boolean reserved) {
        isReserved = reserved;
    }

    public Boolean getDisabled() {
        return isDisabled;
    }

    public void setDisabled(Boolean disabled) {
        isDisabled = disabled;
    }

    public Float getCurrentRangeMeters() {
        return currentRangeMeters;
    }

    public void setCurrentRangeMeters(Float currentRangeMeters) {
        this.currentRangeMeters = currentRangeMeters;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(VehicleType vehicleType) {
        this.vehicleType = vehicleType;
    }

    public PricingPlan getPricingPlan() {
        return pricingPlan;
    }

    public void setPricingPlan(PricingPlan pricingPlan) {
        this.pricingPlan = pricingPlan;
    }
}




