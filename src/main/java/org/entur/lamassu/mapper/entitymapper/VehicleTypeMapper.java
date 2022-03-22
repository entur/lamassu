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

import org.entur.gbfs.v2_3.vehicle_types.GBFSEcoLabel;
import org.entur.gbfs.v2_3.vehicle_types.GBFSVehicleAssets;
import org.entur.gbfs.v2_3.vehicle_types.GBFSVehicleType;
import org.entur.lamassu.model.entities.EcoLabel;
import org.entur.lamassu.model.entities.FormFactor;
import org.entur.lamassu.model.entities.PricingPlan;
import org.entur.lamassu.model.entities.PropulsionType;
import org.entur.lamassu.model.entities.ReturnConstraint;
import org.entur.lamassu.model.entities.VehicleAccessory;
import org.entur.lamassu.model.entities.VehicleAssets;
import org.entur.lamassu.model.entities.VehicleType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class VehicleTypeMapper {

    private final TranslationMapper translationMapper;

    @Autowired
    public VehicleTypeMapper(TranslationMapper translationMapper) {
        this.translationMapper = translationMapper;
    }

    public VehicleType mapVehicleType(GBFSVehicleType vehicleType, List<PricingPlan> pricingPlans, String language) {
        var mapped = new VehicleType();
        mapped.setId(vehicleType.getVehicleTypeId());
        mapped.setFormFactor(FormFactor.valueOf(vehicleType.getFormFactor().name()));
        mapped.setRiderCapacity(vehicleType.getRiderCapacity());
        mapped.setCargoVolumeCapacity(vehicleType.getCargoVolumeCapacity());
        mapped.setCargoLoadCapacity(vehicleType.getCargoLoadCapacity());
        mapped.setPropulsionType(PropulsionType.valueOf(vehicleType.getPropulsionType().name()));
        mapped.setEcoLabel(mapEcoLabels(vehicleType.getEcoLabel()));
        mapped.setMaxRangeMeters(vehicleType.getMaxRangeMeters());
        mapped.setName(translationMapper.mapSingleTranslation(language, vehicleType.getName()));

        mapped.setVehicleAccessories(mapVehicleAccessories(vehicleType.getVehicleAccessories()));

        // TODO error in json schema
        mapped.setgCO2km(vehicleType.getgCO2Km() != null ? vehicleType.getgCO2Km().intValue() : null);

        mapped.setVehicleImage(vehicleType.getVehicleImage());
        mapped.setMake(vehicleType.getMake());
        mapped.setModel(vehicleType.getModel());
        mapped.setColor(vehicleType.getColor());

        // TODO error in json schema
        mapped.setWheelCount(vehicleType.getWheelCount() != null ? vehicleType.getWheelCount().intValue() : null);

        // TODO error in json schema
        mapped.setMaxPermittedSpeed(vehicleType.getMaxPermittedSpeed() != null ? vehicleType.getMaxPermittedSpeed().intValue() : null);

        // TODO error in json schema
        mapped.setRatedPower(vehicleType.getRatedPower() != null ? vehicleType.getRatedPower().intValue() : null);

        mapped.setDefaultReserveTime(vehicleType.getDefaultReserveTime());
        mapped.setReturnConstraint(mapReturnConstraint(vehicleType.getReturnConstraint()));
        mapped.setVehicleAssets(mapVehicleAssets(vehicleType.getVehicleAssets()));
        mapped.setDefaultPricingPlan(vehicleType.getDefaultPricingPlanId() != null ? getPricingPlanWithId(pricingPlans, vehicleType.getDefaultPricingPlanId()) : null);
        mapped.setPricingPlans(mapPricingPlans(vehicleType.getPricingPlanIds(), pricingPlans));
        return mapped;
    }

    private List<PricingPlan> mapPricingPlans(List<String> pricingPlanIds, List<PricingPlan> pricingPlans) {
        if (pricingPlans == null || pricingPlanIds == null) {
            return null;
        }

        return pricingPlanIds.stream().map(id -> getPricingPlanWithId(pricingPlans, id)).collect(Collectors.toList());
    }

    private PricingPlan getPricingPlanWithId(List<PricingPlan> pricingPlans, String id) {
        if (pricingPlans == null || id == null) {
            return null;
        }

        return pricingPlans.stream().filter(pricingPlan -> pricingPlan.getId().equals(id)).findFirst().orElse(null);
    }

    private VehicleAssets mapVehicleAssets(GBFSVehicleAssets vehicleAssets) {
        if (vehicleAssets == null) {
            return null;
        }

        var mapped = new VehicleAssets();
        mapped.setIconUrl(vehicleAssets.getIconUrl());
        mapped.setIconLastModified(vehicleAssets.getIconLastModified());
        mapped.setIconUrlDark(vehicleAssets.getIconUrlDark());
        return mapped;
    }

    private ReturnConstraint mapReturnConstraint(GBFSVehicleType.ReturnConstraint returnConstraint) {
        if (returnConstraint == null) {
            return null;
        }

        return ReturnConstraint.valueOf(returnConstraint.value().toUpperCase());
    }

    private List<VehicleAccessory> mapVehicleAccessories(List<org.entur.gbfs.v2_3.vehicle_types.VehicleAccessory> vehicleAccessories) {
        if (vehicleAccessories == null) {
            return null;
        }

        return vehicleAccessories.stream().map(vehicleAccessory -> VehicleAccessory.valueOf(vehicleAccessory.value().toUpperCase())).collect(Collectors.toList());
    }

    private List<EcoLabel> mapEcoLabels(List<GBFSEcoLabel> ecoLabel) {
        if (ecoLabel == null) {
            return null;
        }

        return ecoLabel.stream().map(this::mapEcoLabel).collect(Collectors.toList());
    }

    private EcoLabel mapEcoLabel(GBFSEcoLabel gbfsEcoLabel) {
        var mapped = new EcoLabel();
        mapped.setEcoSticker(gbfsEcoLabel.getEcoSticker());
        mapped.setCountryCode(gbfsEcoLabel.getCountryCode());
        return mapped;
    }
}
