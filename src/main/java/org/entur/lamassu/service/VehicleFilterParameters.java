package org.entur.lamassu.service;

import java.util.List;
import org.entur.lamassu.model.entities.FormFactor;
import org.entur.lamassu.model.entities.PropulsionType;

public class VehicleFilterParameters extends FilterParameters {

  private List<FormFactor> formFactors;
  private List<PropulsionType> propulsionTypes;
  private boolean includeReserved;
  private boolean includeDisabled;

  public VehicleFilterParameters(
    List<String> codespaces,
    List<String> systems,
    List<String> operators,
    Integer count,
    List<FormFactor> formFactors,
    List<PropulsionType> propulsionTypes,
    boolean includeReserved,
    boolean includeDisabled
  ) {
    super(codespaces, systems, operators, count);
    this.formFactors = formFactors;
    this.propulsionTypes = propulsionTypes;
    this.includeReserved = includeReserved;
    this.includeDisabled = includeDisabled;
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
