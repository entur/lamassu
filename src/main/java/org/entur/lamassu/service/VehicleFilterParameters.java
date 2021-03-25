package org.entur.lamassu.service;

import org.entur.lamassu.model.entities.FormFactor;
import org.entur.lamassu.model.entities.PropulsionType;

import java.util.List;

public class VehicleFilterParameters extends FilterParameters {
    private List<FormFactor> formFactors;
    private List<PropulsionType> propulsionTypes;
    private boolean includeReserved;
    private boolean includeDisabled;

    public List<FormFactor> getFormFactors() {
        return formFactors;
    }

    public void setFormFactors(List<FormFactor> formFactors) {
        this.formFactors = formFactors;
    }

    public List<PropulsionType> getPropulsionTypes() {
        return propulsionTypes;
    }

    public void setPropulsionTypes(List<PropulsionType> propulsionTypes) {
        this.propulsionTypes = propulsionTypes;
    }

    public boolean getIncludeReserved() {
        return includeReserved;
    }

    public void setIncludeReserved(boolean includeReserved) {
        this.includeReserved = includeReserved;
    }

    public boolean getIncludeDisabled() {
        return includeDisabled;
    }

    public void setIncludeDisabled(boolean includeDisabled) {
        this.includeDisabled = includeDisabled;
    }
}
