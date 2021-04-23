package org.entur.lamassu.mapper;

import org.entur.lamassu.model.entities.FormFactor;
import org.entur.lamassu.model.entities.PropulsionType;
import org.entur.lamassu.model.entities.TranslatedString;
import org.entur.lamassu.model.entities.Translation;
import org.entur.lamassu.model.entities.VehicleType;
import org.entur.lamassu.model.gbfs.v2_1.VehicleTypes;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class VehicleTypeMapper {
    public VehicleType mapVehicleType(VehicleTypes.VehicleType vehicleType, String language) {
        var mapped = new VehicleType();
        mapped.setId(vehicleType.getVehicleTypeId());
        mapped.setFormFactor(FormFactor.valueOf(vehicleType.getFormFactor().name()));
        mapped.setPropulsionType(PropulsionType.valueOf(vehicleType.getPropulsionType().name()));
        mapped.setMaxRangeMeters(vehicleType.getMaxRangeMeters());
        mapped.setName(mapTranslation(vehicleType.getName(), language));
        return mapped;
    }

    private Translation mapTranslation(String value, String language) {
        var translation = new Translation();
        var translatedString = new TranslatedString();
        translatedString.setLanguage(language);
        translatedString.setValue(value);
        translation.setTranslation(List.of(translatedString));
        return translation;
    }
}
