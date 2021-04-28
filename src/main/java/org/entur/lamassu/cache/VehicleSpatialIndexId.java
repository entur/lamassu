package org.entur.lamassu.cache;

import org.entur.lamassu.model.entities.FormFactor;
import org.entur.lamassu.model.entities.PropulsionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VehicleSpatialIndexId {
    private static final Logger logger = LoggerFactory.getLogger(VehicleSpatialIndexId.class);
    private String vehicleId;
    private String codespace;
    private String operatorId;
    private String systemId;
    private FormFactor formFactor;
    private PropulsionType propulsionTypes;
    private boolean isReserved;
    private boolean isDisabled;

    public static VehicleSpatialIndexId fromString(String indexId) {
        try {
            var parsed = new VehicleSpatialIndexId();
            var parts = indexId.split("_");
            parsed.setVehicleId(parts[0]);
            parsed.setCodespace(parts[1]);
            parsed.setSystemId(parts[2]);
            parsed.setOperatorId(parts[3]);
            parsed.setFormFactor(FormFactor.valueOf(parts[4]));
            parsed.setPropulsionTypes(PropulsionType.valueOf(parts[5]));
            parsed.setReserved(Boolean.parseBoolean(parts[6]));
            parsed.setDisabled(Boolean.parseBoolean(parts[7]));
            return parsed;
        } catch (IndexOutOfBoundsException e) {
            logger.warn("Caught IndexOutOfBoundsException while trying to parse spatial index id {}", indexId, e);
            return null;
        }
    }

    @Override
    public String toString() {
        return vehicleId + '_' +
                codespace + '_' +
                systemId + '_' +
                operatorId + '_' +
                formFactor + '_' +
                propulsionTypes + '_' +
                isReserved + '_' +
                isDisabled;
    }

    private VehicleSpatialIndexId() {}

    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(String operatorId) {
        this.operatorId = operatorId;
    }

    public String getCodespace() {
        return codespace;
    }

    public void setCodespace(String codespace) {
        this.codespace = codespace;
    }

    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }

    public String getSystemId() {
        return systemId;
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
