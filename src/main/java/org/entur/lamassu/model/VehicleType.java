package org.entur.lamassu.model;

public class VehicleType implements Entity {
    private String id;
    private FormFactor formFactor;
    private PropulsionType propulsionType;
    private Float maxRangeMeters;
    private String name;

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

    public PropulsionType getPropulsionType() {
        return propulsionType;
    }

    public void setPropulsionType(PropulsionType propulsionType) {
        this.propulsionType = propulsionType;
    }

    public Float getMaxRangeMeters() {
        return maxRangeMeters;
    }

    public void setMaxRangeMeters(Float maxRangeMeters) {
        this.maxRangeMeters = maxRangeMeters;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
