package org.entur.lamassu.mapper;

import org.entur.lamassu.model.FormFactor;
import org.entur.lamassu.model.PropulsionType;
import org.entur.lamassu.model.VehicleType;
import org.entur.lamassu.model.gbfs.v2_1.VehicleTypes;
import org.springframework.stereotype.Component;

@Component
public class VehicleTypeMapper {
    public VehicleType mapVehicleType(VehicleTypes.VehicleType vehicleType) {
        var mapped = new VehicleType();
        mapped.setId(vehicleType.getVehicleTypeId());
        mapped.setFormFactor(FormFactor.valueOf(vehicleType.getFormFactor().name()));
        mapped.setPropulsionType(PropulsionType.valueOf(vehicleType.getPropulsionType().name()));
        mapped.setMaxRangeMeters(vehicleType.getMaxRangeMeters());
        mapped.setName(vehicleType.getName());
        return mapped;
    }
}
