package org.entur.lamassu.model.entities;

import java.util.List;

public class VehicleType implements Entity {
    private String id;
    private FormFactor formFactor;
    private Integer riderCapacity;
    private Integer cargoVolumeCapacity;
    private Integer cargoLoadCapacity;
    private PropulsionType propulsionType;
    private List<EcoLabel> ecoLabel;
    private Double maxRangeMeters;
    private TranslatedString name;
    private List<VehicleAccessory> vehicleAccessories;
    private Integer gCO2km;
    private String vehicleImage;
    private String make;
    private String model;
    private String color;
    private Integer wheelCount;
    private Integer maxPermittedSpeed;
    private Integer ratedPower;
    private Integer defaultReserveTime;
    private ReturnConstraint returnConstraint;
    private VehicleAssets vehicleAssets;
    private PricingPlan defaultPricingPlan;
    private List<PricingPlan> pricingPlans;

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public FormFactor getFormFactor() {
        return formFactor;
    }

    public void setFormFactor(FormFactor formFactor) {
        this.formFactor = formFactor;
    }

    public Integer getRiderCapacity() {
        return riderCapacity;
    }

    public void setRiderCapacity(Integer riderCapacity) {
        this.riderCapacity = riderCapacity;
    }

    public Integer getCargoVolumeCapacity() {
        return cargoVolumeCapacity;
    }

    public void setCargoVolumeCapacity(Integer cargoVolumeCapacity) {
        this.cargoVolumeCapacity = cargoVolumeCapacity;
    }

    public Integer getCargoLoadCapacity() {
        return cargoLoadCapacity;
    }

    public void setCargoLoadCapacity(Integer cargoLoadCapacity) {
        this.cargoLoadCapacity = cargoLoadCapacity;
    }

    public PropulsionType getPropulsionType() {
        return propulsionType;
    }

    public void setPropulsionType(PropulsionType propulsionType) {
        this.propulsionType = propulsionType;
    }

    public List<EcoLabel> getEcoLabel() {
        return ecoLabel;
    }

    public void setEcoLabel(List<EcoLabel> ecoLabel) {
        this.ecoLabel = ecoLabel;
    }

    public Double getMaxRangeMeters() {
        return maxRangeMeters;
    }

    public void setMaxRangeMeters(Double maxRangeMeters) {
        this.maxRangeMeters = maxRangeMeters;
    }

    public TranslatedString getName() {
        return name;
    }

    public void setName(TranslatedString name) {
        this.name = name;
    }

    public List<VehicleAccessory> getVehicleAccessories() {
        return vehicleAccessories;
    }

    public void setVehicleAccessories(List<VehicleAccessory> vehicleAccessories) {
        this.vehicleAccessories = vehicleAccessories;
    }

    public Integer getgCO2km() {
        return gCO2km;
    }

    public void setgCO2km(Integer gCO2km) {
        this.gCO2km = gCO2km;
    }

    public String getVehicleImage() {
        return vehicleImage;
    }

    public void setVehicleImage(String vehicleImage) {
        this.vehicleImage = vehicleImage;
    }

    public String getMake() {
        return make;
    }

    public void setMake(String make) {
        this.make = make;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Integer getWheelCount() {
        return wheelCount;
    }

    public void setWheelCount(Integer wheelCount) {
        this.wheelCount = wheelCount;
    }

    public Integer getMaxPermittedSpeed() {
        return maxPermittedSpeed;
    }

    public void setMaxPermittedSpeed(Integer maxPermittedSpeed) {
        this.maxPermittedSpeed = maxPermittedSpeed;
    }

    public Integer getRatedPower() {
        return ratedPower;
    }

    public void setRatedPower(Integer ratedPower) {
        this.ratedPower = ratedPower;
    }

    public Integer getDefaultReserveTime() {
        return defaultReserveTime;
    }

    public void setDefaultReserveTime(Integer defaultReserveTime) {
        this.defaultReserveTime = defaultReserveTime;
    }

    public ReturnConstraint getReturnConstraint() {
        return returnConstraint;
    }

    public void setReturnConstraint(ReturnConstraint returnConstraint) {
        this.returnConstraint = returnConstraint;
    }

    public VehicleAssets getVehicleAssets() {
        return vehicleAssets;
    }

    public void setVehicleAssets(VehicleAssets vehicleAssets) {
        this.vehicleAssets = vehicleAssets;
    }

    public PricingPlan getDefaultPricingPlan() {
        return defaultPricingPlan;
    }

    public void setDefaultPricingPlan(PricingPlan defaultPricingPlan) {
        this.defaultPricingPlan = defaultPricingPlan;
    }

    public List<PricingPlan> getPricingPlans() {
        return pricingPlans;
    }

    public void setPricingPlans(List<PricingPlan> pricingPlans) {
        this.pricingPlans = pricingPlans;
    }

    @Override
    public String toString() {
        return "VehicleType{" +
                "id='" + id + '\'' +
                ", formFactor=" + formFactor +
                ", riderCapacity=" + riderCapacity +
                ", cargoVolumeCapacity=" + cargoVolumeCapacity +
                ", cargoLoadCapacity=" + cargoLoadCapacity +
                ", propulsionType=" + propulsionType +
                ", ecoLabel=" + ecoLabel +
                ", maxRangeMeters=" + maxRangeMeters +
                ", name=" + name +
                ", vehicleAccessories=" + vehicleAccessories +
                ", gCO2km=" + gCO2km +
                ", vehicleImage='" + vehicleImage + '\'' +
                ", make='" + make + '\'' +
                ", model='" + model + '\'' +
                ", color='" + color + '\'' +
                ", wheelCount=" + wheelCount +
                ", maxPermittedSpeed=" + maxPermittedSpeed +
                ", ratedPower=" + ratedPower +
                ", defaultReserveTime=" + defaultReserveTime +
                ", returnConstraint=" + returnConstraint +
                ", vehicleAssets=" + vehicleAssets +
                ", defaultPricingPlan=" + defaultPricingPlan +
                ", pricingPlans=" + pricingPlans +
                '}';
    }
}
