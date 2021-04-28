package org.entur.lamassu.cache;

import org.entur.lamassu.model.entities.FormFactor;
import org.entur.lamassu.model.entities.PropulsionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VehicleSpatialIndexId extends AbstractSpatialIndexId {
    private static final Logger logger = LoggerFactory.getLogger(VehicleSpatialIndexId.class);
    private FormFactor formFactor;
    private PropulsionType propulsionTypes;
    private boolean isReserved;
    private boolean isDisabled;

    public static VehicleSpatialIndexId fromString(String indexId) {
        try {
            var parsed = new VehicleSpatialIndexId();
            var parts = indexId.split("_");
            parsed.parse(parts);
            return parsed;
        } catch (IndexOutOfBoundsException e) {
            logger.warn("Caught IndexOutOfBoundsException while trying to parse spatial index id {}", indexId, e);
            return null;
        }
    }

    @Override
    public void parse(String[] parts) {
        super.parse(parts);
        setFormFactor(FormFactor.valueOf(parts[4]));
        setPropulsionTypes(PropulsionType.valueOf(parts[5]));
        setReserved(Boolean.parseBoolean(parts[6]));
        setDisabled(Boolean.parseBoolean(parts[7]));
    }

    @Override
    public String toString() {
        return getId() + '_' +
                getCodespace() + '_' +
                getSystemId() + '_' +
                getOperatorId() + '_' +
                getFormFactor() + '_' +
                getPropulsionTypes() + '_' +
                getReserved() + '_' +
                getDisabled();
    }

    private VehicleSpatialIndexId() {}

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
