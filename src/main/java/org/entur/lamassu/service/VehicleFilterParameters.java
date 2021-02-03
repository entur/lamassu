package org.entur.lamassu.service;

import org.entur.lamassu.model.FormFactor;
import org.entur.lamassu.model.PropulsionType;

import java.util.List;

public class VehicleFilterParameters {
    private List<String> operators;
    private List<String> codespaces;
    private List<FormFactor> formFactors;
    private List<PropulsionType> propulsionTypes;
    private Boolean includeReserved;
    private Boolean includeDisabled;

    public List<String> getOperators() {
        return operators;
    }

    public void setOperators(List<String> operators) {
        this.operators = operators;
    }

    public List<String> getCodespaces() {
        return codespaces;
    }

    public void setCodespaces(List<String> codespaces) {
        this.codespaces = codespaces;
    }

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

    public Boolean getIncludeReserved() {
        return includeReserved;
    }

    public void setIncludeReserved(Boolean includeReserved) {
        this.includeReserved = includeReserved;
    }

    public Boolean getIncludeDisabled() {
        return includeDisabled;
    }

    public void setIncludeDisabled(Boolean includeDisabled) {
        this.includeDisabled = includeDisabled;
    }
}
