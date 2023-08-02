package org.entur.lamassu.cache.impl;

import org.entur.lamassu.cache.VehicleSpatialIndex;
import org.entur.lamassu.cache.VehicleSpatialIndexId;
import org.entur.lamassu.model.entities.Vehicle;
import org.redisson.api.RGeo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class VehicleSpatialIndexImpl
  extends SpatialIndexImpl<VehicleSpatialIndexId, Vehicle>
  implements VehicleSpatialIndex {

  @Autowired
  public VehicleSpatialIndexImpl(RGeo<VehicleSpatialIndexId> vehicleSpatialIndex) {
    super(vehicleSpatialIndex);
  }
}
