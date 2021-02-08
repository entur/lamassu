package org.entur.lamassu.cache;

import org.entur.lamassu.model.entities.FormFactor;
import org.entur.lamassu.model.entities.PropulsionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpatialIndexId {
    private static final Logger logger = LoggerFactory.getLogger(SpatialIndexId.class);
    private String vehicleId;
    private String operator;
    private String codespace;
    private FormFactor formFactor;
    private PropulsionType propulsionTypes;
    private boolean isReserved;
    private boolean isDisabled;

    public static SpatialIndexId fromString(String indexId) {
        try {
            var parsed = new SpatialIndexId();
            var parts = indexId.split("_");
            parsed.setVehicleId(parts[0]);
            parsed.setOperator(parts[1]);
            parsed.setCodespace(parts[2]);
            parsed.setFormFactor(FormFactor.valueOf(parts[3]));
            parsed.setPropulsionTypes(PropulsionType.valueOf(parts[4]));
            parsed.setReserved(Boolean.parseBoolean(parts[5]));
            parsed.setDisabled(Boolean.parseBoolean(parts[6]));
            return parsed;
        } catch (IndexOutOfBoundsException e) {
            logger.warn("Caught IndexOutOfBoundsException while trying to parse spatial index id {}", indexId, e);
            return null;
        }
    }

    @Override
    public String toString() {
        return vehicleId + '_' +
                operator + '_' +
                codespace + '_' +
                formFactor + '_' +
                propulsionTypes + '_' +
                isReserved + '_' +
                isDisabled;
    }

    private SpatialIndexId() {}

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

    public boolean getReserved() {
        return isReserved;
    }

    public void setReserved(boolean reserved) {
        isReserved = reserved;
    }

    public boolean getDisabled() {
        return isDisabled;
    }

    public void setDisabled(boolean disabled) {
        isDisabled = disabled;
    }
}
