package org.entur.lamassu.cache;

import org.entur.lamassu.model.entities.FormFactor;
import org.entur.lamassu.model.entities.PropulsionType;

public class VehicleSpatialIndexId
  extends AbstractSpatialIndexId
  implements SpatialIndexId {

  private FormFactor formFactor;
  private PropulsionType propulsionType;
  private boolean isReserved;
  private boolean isDisabled;

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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    VehicleSpatialIndexId that = (VehicleSpatialIndexId) o;

    if (isReserved != that.isReserved) return false;
    if (isDisabled != that.isDisabled) return false;
    if (formFactor != that.formFactor) return false;
    return propulsionType == that.propulsionType;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (formFactor != null ? formFactor.hashCode() : 0);
    result = 31 * result + (propulsionType != null ? propulsionType.hashCode() : 0);
    result = 31 * result + (isReserved ? 1 : 0);
    result = 31 * result + (isDisabled ? 1 : 0);
    return result;
  }
}
