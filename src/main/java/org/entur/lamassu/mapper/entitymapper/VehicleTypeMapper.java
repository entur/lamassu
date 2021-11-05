/*
 *
 *
 *  * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 *  * the European Commission - subsequent versions of the EUPL (the "Licence");
 *  * You may not use this work except in compliance with the Licence.
 *  * You may obtain a copy of the Licence at:
 *  *
 *  *   https://joinup.ec.europa.eu/software/page/eupl
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the Licence is distributed on an "AS IS" basis,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the Licence for the specific language governing permissions and
 *  * limitations under the Licence.
 *
 */

package org.entur.lamassu.mapper.entitymapper;

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
