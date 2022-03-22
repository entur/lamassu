package org.entur.lamassu.model.entities;

import java.util.List;
import java.util.Objects;

public class Vehicle implements LocationEntity {
    private String id;
    private Double lat;
    private Double lon;
    private Boolean isReserved;
    private Boolean isDisabled;
    private Double currentRangeMeters;
    private Double currentFuelPercent;
    private VehicleType vehicleType;
    private List<VehicleEquipment> vehicleEquipment;
    private String availableUntil;
    private PricingPlan pricingPlan;
    private System system;
    private RentalUris rentalUris;

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    @Override
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

    public Double getCurrentRangeMeters() {
        return currentRangeMeters;
    }

    public void setCurrentRangeMeters(Double currentRangeMeters) {
        this.currentRangeMeters = currentRangeMeters;
    }

    public Double getCurrentFuelPercent() {
        return currentFuelPercent;
    }

    public void setCurrentFuelPercent(Double currentFuelPercent) {
        this.currentFuelPercent = currentFuelPercent;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(VehicleType vehicleType) {
        this.vehicleType = vehicleType;
    }

    public List<VehicleEquipment> getVehicleEquipment() {
        return vehicleEquipment;
    }

    public void setVehicleEquipment(List<VehicleEquipment> vehicleEquipment) {
        this.vehicleEquipment = vehicleEquipment;
    }

    public String getAvailableUntil() {
        return availableUntil;
    }

    public void setAvailableUntil(String availableUntil) {
        this.availableUntil = availableUntil;
    }

    public PricingPlan getPricingPlan() {
        return pricingPlan;
    }

    public void setPricingPlan(PricingPlan pricingPlan) {
        this.pricingPlan = pricingPlan;
    }

    public System getSystem() {
        return system;
    }

    public void setSystem(System system) {
        this.system = system;
    }

    public RentalUris getRentalUris() {
        return rentalUris;
    }

    public void setRentalUris(RentalUris rentalUris) {
        this.rentalUris = rentalUris;
    }

    @Override
    public String toString() {
        return "Vehicle{" +
                "id='" + id + '\'' +
                ", lat=" + lat +
                ", lon=" + lon +
                ", isReserved=" + isReserved +
                ", isDisabled=" + isDisabled +
                ", currentRangeMeters=" + currentRangeMeters +
                ", currentFuelPercent=" + currentFuelPercent +
                ", vehicleType=" + vehicleType +
                ", vehicleEquipment=" + vehicleEquipment +
                ", availableUntil='" + availableUntil + '\'' +
                ", pricingPlan=" + pricingPlan +
                ", system=" + system +
                ", rentalUris=" + rentalUris +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vehicle)) return false;
        Vehicle vehicle = (Vehicle) o;
        return getId().equals(vehicle.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}





