package org.entur.lamassu.mapper;

import org.entur.gbfs.v2_2.vehicle_types.GBFSVehicleType;
import org.entur.lamassu.model.entities.FormFactor;
import org.entur.lamassu.model.entities.PropulsionType;
import org.entur.lamassu.model.entities.VehicleType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class VehicleTypeMapper {

    private final TranslationMapper translationMapper;

    @Autowired
    public VehicleTypeMapper(TranslationMapper translationMapper) {
        this.translationMapper = translationMapper;
    }

    public VehicleType mapVehicleType(GBFSVehicleType vehicleType, String language) {
        var mapped = new VehicleType();
        mapped.setId(vehicleType.getVehicleTypeId());
        mapped.setFormFactor(FormFactor.valueOf(vehicleType.getFormFactor().name()));
        mapped.setPropulsionType(PropulsionType.valueOf(vehicleType.getPropulsionType().name()));
        mapped.setMaxRangeMeters(vehicleType.getMaxRangeMeters());
        mapped.setName(translationMapper.mapSingleTranslation(language, vehicleType.getName()));
        return mapped;
    }
}
