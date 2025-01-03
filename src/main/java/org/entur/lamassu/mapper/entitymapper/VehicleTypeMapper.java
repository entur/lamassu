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

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.entur.lamassu.model.entities.EcoLabel;
import org.entur.lamassu.model.entities.FormFactor;
import org.entur.lamassu.model.entities.PropulsionType;
import org.entur.lamassu.model.entities.ReturnConstraint;
import org.entur.lamassu.model.entities.VehicleAccessory;
import org.entur.lamassu.model.entities.VehicleAssets;
import org.entur.lamassu.model.entities.VehicleType;
import org.mobilitydata.gbfs.v3_0.vehicle_types.GBFSEcoLabel;
import org.mobilitydata.gbfs.v3_0.vehicle_types.GBFSMake;
import org.mobilitydata.gbfs.v3_0.vehicle_types.GBFSModel;
import org.mobilitydata.gbfs.v3_0.vehicle_types.GBFSVehicleAssets;
import org.mobilitydata.gbfs.v3_0.vehicle_types.GBFSVehicleType;
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
    mapped.setRiderCapacity(vehicleType.getRiderCapacity());
    mapped.setCargoVolumeCapacity(vehicleType.getCargoVolumeCapacity());
    mapped.setCargoLoadCapacity(vehicleType.getCargoLoadCapacity());
    mapped.setPropulsionType(
      PropulsionType.valueOf(vehicleType.getPropulsionType().name())
    );
    mapped.setEcoLabel(mapEcoLabels(vehicleType.getEcoLabels()));
    mapped.setEcoLabels(mapEcoLabels(vehicleType.getEcoLabels()));
    mapped.setMaxRangeMeters(vehicleType.getMaxRangeMeters());
    mapped.setName(
      translationMapper.mapTranslatedString(
        Optional
          .ofNullable(vehicleType.getName())
          .orElse(Collections.emptyList())
          .stream()
          .map(name ->
            translationMapper.mapTranslation(name.getLanguage(), name.getText())
          )
          .toList()
      )
    );
    mapped.setDescription(
      translationMapper.mapTranslatedString(
        Optional
          .ofNullable(vehicleType.getDescription())
          .orElse(Collections.emptyList())
          .stream()
          .map(description ->
            translationMapper.mapTranslation(
              description.getLanguage(),
              description.getText()
            )
          )
          .toList()
      )
    );
    mapped.setVehicleAccessories(
      mapVehicleAccessories(vehicleType.getVehicleAccessories())
    );
    mapped.setgCO2km(vehicleType.getgCO2Km() != null ? vehicleType.getgCO2Km() : null);
    mapped.setVehicleImage(vehicleType.getVehicleImage());
    mapped.setMake(
      Optional
        .ofNullable(vehicleType.getMake())
        .orElse(Collections.emptyList())
        .stream()
        .filter(make -> make.getLanguage().equals(language))
        .map(GBFSMake::getText)
        .findFirst()
        .orElse(null)
    );
    mapped.setModel(
      Optional
        .ofNullable(vehicleType.getModel())
        .orElse(Collections.emptyList())
        .stream()
        .filter(model -> model.getLanguage().equals(language))
        .map(GBFSModel::getText)
        .findFirst()
        .orElse(null)
    );
    mapped.setColor(vehicleType.getColor());
    mapped.setWheelCount(
      vehicleType.getWheelCount() != null ? vehicleType.getWheelCount() : null
    );
    mapped.setMaxPermittedSpeed(
      vehicleType.getMaxPermittedSpeed() != null
        ? vehicleType.getMaxPermittedSpeed()
        : null
    );
    mapped.setRatedPower(
      vehicleType.getRatedPower() != null ? vehicleType.getRatedPower() : null
    );
    mapped.setDefaultReserveTime(vehicleType.getDefaultReserveTime());
    mapped.setReturnConstraint(mapReturnConstraint(vehicleType.getReturnConstraint()));
    mapped.setVehicleAssets(mapVehicleAssets(vehicleType.getVehicleAssets()));
    mapped.setDefaultPricingPlanId(vehicleType.getDefaultPricingPlanId());
    mapped.setPricingPlanIds(vehicleType.getPricingPlanIds());
    return mapped;
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

  private ReturnConstraint mapReturnConstraint(
    GBFSVehicleType.ReturnConstraint returnConstraint
  ) {
    if (returnConstraint == null) {
      return null;
    }

    return ReturnConstraint.valueOf(returnConstraint.value().toUpperCase());
  }

  private List<VehicleAccessory> mapVehicleAccessories(
    List<org.mobilitydata.gbfs.v3_0.vehicle_types.VehicleAccessory> vehicleAccessories
  ) {
    if (vehicleAccessories == null) {
      return null;
    }

    return vehicleAccessories
      .stream()
      .map(vehicleAccessory ->
        VehicleAccessory.valueOf(vehicleAccessory.value().toUpperCase())
      )
      .toList();
  }

  private List<EcoLabel> mapEcoLabels(List<GBFSEcoLabel> ecoLabel) {
    if (ecoLabel == null) {
      return null;
    }

    return ecoLabel.stream().map(this::mapEcoLabel).toList();
  }

  private EcoLabel mapEcoLabel(GBFSEcoLabel gbfsEcoLabel) {
    var mapped = new EcoLabel();
    mapped.setEcoSticker(gbfsEcoLabel.getEcoSticker());
    mapped.setCountryCode(gbfsEcoLabel.getCountryCode());
    return mapped;
  }
}
