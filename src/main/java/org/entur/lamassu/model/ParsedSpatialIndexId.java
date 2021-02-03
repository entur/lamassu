package org.entur.lamassu.model;

public class ParsedSpatialIndexId {
    private String vehicleId;
    private String operator;
    private String codespace;
    private FormFactor formFactor;
    private PropulsionType propulsionTypes;
    private Boolean isReserved;
    private Boolean isDisabled;

    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getCodespace() {
        return codespace;
    }

    public void setCodespace(String codespace) {
        this.codespace = codespace;
    }

    public FormFactor getFormFactor() {
        return formFactor;
    }

    public void setFormFactor(FormFactor formFactor) {
        this.formFactor = formFactor;
    }

    public PropulsionType getPropulsionTypes() {
        return propulsionTypes;
    }

    public void setPropulsionTypes(PropulsionType propulsionTypes) {
        this.propulsionTypes = propulsionTypes;
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
}
