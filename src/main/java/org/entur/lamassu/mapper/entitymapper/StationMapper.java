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
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.entur.lamassu.model.entities.MultiPolygon;
import org.entur.lamassu.model.entities.ParkingType;
import org.entur.lamassu.model.entities.PricingPlan;
import org.entur.lamassu.model.entities.Region;
import org.entur.lamassu.model.entities.RentalMethod;
import org.entur.lamassu.model.entities.Station;
import org.entur.lamassu.model.entities.System;
import org.entur.lamassu.model.entities.VehicleDocksAvailability;
import org.entur.lamassu.model.entities.VehicleType;
import org.entur.lamassu.model.entities.VehicleTypeAvailability;
import org.entur.lamassu.model.entities.VehicleTypeCapacity;
import org.jetbrains.annotations.NotNull;
import org.mobilitydata.gbfs.v3_0.station_information.GBFSName;
import org.mobilitydata.gbfs.v3_0.station_information.GBFSShortName;
import org.mobilitydata.gbfs.v3_0.station_information.GBFSStation;
import org.mobilitydata.gbfs.v3_0.station_information.GBFSVehicleDocksCapacity;
import org.mobilitydata.gbfs.v3_0.station_information.GBFSVehicleTypesCapacity;
import org.mobilitydata.gbfs.v3_0.station_status.GBFSVehicleDocksAvailable;
import org.mobilitydata.gbfs.v3_0.station_status.GBFSVehicleTypesAvailable;
import org.mobilitydata.gbfs.v3_0.system_regions.GBFSSystemRegions;
import org.mobilitydata.gbfs.v3_0.vehicle_types.GBFSVehicleTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StationMapper {

  private final TranslationMapper translationMapper;
  private final RentalUrisMapper rentalUrisMapper;
  private final VehicleTypeMapper vehicleTypeMapper;

  private static final Logger logger = LoggerFactory.getLogger(StationMapper.class);

  @Autowired
  public StationMapper(
    TranslationMapper translationMapper,
    RentalUrisMapper rentalUrisMapper,
    VehicleTypeMapper vehicleTypeMapper
  ) {
    this.translationMapper = translationMapper;
    this.rentalUrisMapper = rentalUrisMapper;
    this.vehicleTypeMapper = vehicleTypeMapper;
  }

  public Station mapStation(
    System system,
    List<PricingPlan> pricingPlans,
    GBFSStation stationInformation,
    org.mobilitydata.gbfs.v3_0.station_status.GBFSStation stationStatus,
    GBFSVehicleTypes vehicleTypesFeed,
    GBFSSystemRegions regions,
    String language
  ) {
    var station = new Station();
    station.setId(stationStatus.getStationId());
    station.setName(
      translationMapper.mapSingleTranslation(
        language,
        stationInformation
          .getName()
          .stream()
          .filter(name -> name.getLanguage().equals(language))
          .map(GBFSName::getText)
          .findFirst()
          .orElse(null)
      )
    );
    station.setShortName(
      translationMapper.mapSingleTranslation(
        language,
        Optional
          .ofNullable(stationInformation.getShortName())
          .orElse(Collections.emptyList())
          .stream()
          .filter(shortName -> shortName.getLanguage().equals(language))
          .map(GBFSShortName::getText)
          .findFirst()
          .orElse(null)
      )
    );
    station.setLat(stationInformation.getLat());
    station.setLon(stationInformation.getLon());
    station.setAddress(stationInformation.getAddress());
    station.setCrossStreet(stationInformation.getCrossStreet());
    station.setRegion(mapRegion(regions, stationInformation.getRegionId(), language));
    station.setPostCode(stationInformation.getPostCode());
    station.setRentalMethods(mapRentalMethods(stationInformation.getRentalMethods()));
    station.setVirtualStation(stationInformation.getIsVirtualStation());
    station.setStationArea(mapStationArea(stationInformation.getStationArea()));
    station.setParkingType(mapParkingType(stationInformation.getParkingType()));
    station.setParkingHoop(stationInformation.getParkingHoop());
    station.setContactPhone(stationInformation.getContactPhone());
    station.setCapacity(
      stationInformation.getCapacity() != null ? stationInformation.getCapacity() : null
    );
    station.setVehicleCapacity(
      stationInformation.getVehicleTypesCapacity() != null
        ? mapVehicleCapacities(
          stationInformation.getVehicleTypesCapacity(),
          mapVehicleTypes(vehicleTypesFeed, pricingPlans, language)
        )
        : null
    );
    station.setVehicleTypeCapacity(
      stationInformation.getVehicleDocksCapacity() != null
        ? mapVehicleTypeCapacities(
          stationInformation.getVehicleDocksCapacity(),
          mapVehicleTypes(vehicleTypesFeed, pricingPlans, language)
        )
        : null
    );
    station.setValetStation(stationInformation.getIsValetStation());
    station.setChargingStation(stationInformation.getIsChargingStation());
    station.setRentalUris(
      rentalUrisMapper.mapRentalUris(stationInformation.getRentalUris())
    );
    station.setNumBikesAvailable(
      stationStatus.getNumVehiclesAvailable() != null
        ? stationStatus.getNumVehiclesAvailable()
        : null
    );
    station.setVehicleTypesAvailable(
      stationStatus.getVehicleTypesAvailable() != null
        ? mapVehicleTypesAvailable(
          vehicleTypesFeed,
          stationStatus.getVehicleTypesAvailable(),
          pricingPlans,
          language
        )
        : null
    );
    station.setNumBikesDisabled(
      stationStatus.getNumVehiclesDisabled() != null
        ? stationStatus.getNumVehiclesDisabled()
        : null
    );
    station.setNumDocksAvailable(
      stationStatus.getNumDocksAvailable() != null
        ? stationStatus.getNumDocksAvailable()
        : null
    );
    station.setVehicleDocksAvailable(
      stationStatus.getVehicleDocksAvailable() != null
        ? mapVehicleDocksAvailable(
          vehicleTypesFeed,
          stationStatus.getVehicleDocksAvailable(),
          pricingPlans,
          language
        )
        : null
    );
    station.setNumDocksDisabled(station.getNumDocksDisabled());
    station.setInstalled(stationStatus.getIsInstalled());
    station.setRenting(stationStatus.getIsRenting());
    station.setReturning(stationStatus.getIsReturning());
    station.setLastReported(
      stationStatus.getLastReported() != null
        ? stationStatus.getLastReported().getTime() / 1000
        : null
    );
    station.setSystem(system);
    station.setPricingPlans(pricingPlans);
    return station;
  }

  private ParkingType mapParkingType(GBFSStation.ParkingType parkingType) {
    if (parkingType == null) {
      return null;
    }

    return ParkingType.valueOf(parkingType.value().toUpperCase());
  }

  private MultiPolygon mapStationArea(org.geojson.MultiPolygon stationArea) {
    if (stationArea == null) {
      return null;
    }

    return MultiPolygon.fromGeoJson(stationArea);
  }

  private List<RentalMethod> mapRentalMethods(
    List<org.mobilitydata.gbfs.v3_0.station_information.RentalMethod> rentalMethods
  ) {
    return Optional
      .ofNullable(rentalMethods)
      .map(values ->
        values
          .stream()
          .filter(Objects::nonNull)
          .map(rentalMethod -> RentalMethod.valueOf(rentalMethod.name().toUpperCase()))
          .toList()
      )
      .orElse(null);
  }

  private Region mapRegion(GBFSSystemRegions regions, String regionId, String language) {
    if (regionId == null || regionId.isBlank()) {
      return null;
    }

    var sourceRegion = Optional
      .ofNullable(regions)
      .flatMap(r ->
        r
          .getData()
          .getRegions()
          .stream()
          .filter(region -> region.getRegionId().equals(regionId))
          .findFirst()
      );

    if (sourceRegion.isPresent()) {
      var region = new Region();
      region.setId(regionId);
      region.setName(
        translationMapper.mapSingleTranslation(
          language,
          sourceRegion
            .get()
            .getName()
            .stream()
            .filter(name -> name.getLanguage().equals(language))
            .map(org.mobilitydata.gbfs.v3_0.system_regions.GBFSName::getText)
            .findFirst()
            .orElse(null)
        )
      );
      return region;
    } else {
      logger.warn(
        "Could not map regionId to a region from system_regions feed {}",
        regionId
      );
      return null;
    }
  }

  private List<VehicleTypeCapacity> mapVehicleCapacities(
    List<GBFSVehicleTypesCapacity> vehicleCapacityList,
    Map<String, VehicleType> vehicleTypes
  ) {
    return vehicleCapacityList
      .stream()
      .map(vehicleCapacity -> mapVehicleTypeCapacity(vehicleTypes, vehicleCapacity))
      .toList();
  }

  private static @NotNull VehicleTypeCapacity mapVehicleTypeCapacity(
    Map<String, VehicleType> vehicleTypes,
    GBFSVehicleTypesCapacity vehicleCapacity
  ) {
    var mapped = new VehicleTypeCapacity();
    mapped.setVehicleType(
      vehicleTypes.get(vehicleCapacity.getVehicleTypeIds().getFirst())
    );
    mapped.setVehicleTypes(
      vehicleCapacity.getVehicleTypeIds().stream().map(vehicleTypes::get).toList()
    );
    mapped.setCount(vehicleCapacity.getCount());
    return mapped;
  }

  private List<VehicleTypeCapacity> mapVehicleTypeCapacities(
    List<GBFSVehicleDocksCapacity> vehicleCapacity,
    Map<String, VehicleType> vehicleTypes
  ) {
    return vehicleCapacity
      .stream()
      .map(entry -> mapVehicleDocksCapacity(entry, vehicleTypes))
      .toList();
  }

  private VehicleTypeCapacity mapVehicleDocksCapacity(
    GBFSVehicleDocksCapacity vehicleDocksCapacity,
    Map<String, VehicleType> vehicleTypes
  ) {
    var mapped = new VehicleTypeCapacity();
    mapped.setVehicleType(
      vehicleTypes.get(vehicleDocksCapacity.getVehicleTypeIds().getFirst())
    );
    mapped.setVehicleTypes(
      vehicleDocksCapacity.getVehicleTypeIds().stream().map(vehicleTypes::get).toList()
    );
    mapped.setCount(vehicleDocksCapacity.getCount());
    return mapped;
  }

  private Map<String, VehicleType> mapVehicleTypes(
    GBFSVehicleTypes vehicleTypesFeed,
    List<PricingPlan> pricingPlans,
    String language
  ) {
    return vehicleTypesFeed
      .getData()
      .getVehicleTypes()
      .stream()
      .map(vehicleType ->
        vehicleTypeMapper.mapVehicleType(vehicleType, pricingPlans, language)
      )
      .collect(Collectors.toMap(VehicleType::getId, vehicleType -> vehicleType));
  }

  private List<VehicleTypeAvailability> mapVehicleTypesAvailable(
    GBFSVehicleTypes vehicleTypesFeed,
    List<GBFSVehicleTypesAvailable> vehicleTypesAvailable,
    List<PricingPlan> pricingPlans,
    String language
  ) {
    var mappedVehicleTypes = vehicleTypesFeed
      .getData()
      .getVehicleTypes()
      .stream()
      .map(vehicleType ->
        vehicleTypeMapper.mapVehicleType(vehicleType, pricingPlans, language)
      )
      .collect(Collectors.toMap(VehicleType::getId, vehicleType -> vehicleType));

    return vehicleTypesAvailable
      .stream()
      .map(vehicleTypeAvailability ->
        mapVehicleTypeAvailability(
          mappedVehicleTypes.get(vehicleTypeAvailability.getVehicleTypeId()),
          vehicleTypeAvailability
        )
      )
      .collect(Collectors.toList());
  }

  private VehicleTypeAvailability mapVehicleTypeAvailability(
    VehicleType vehicleType,
    GBFSVehicleTypesAvailable vehicleTypeAvailability
  ) {
    var mapped = new VehicleTypeAvailability();
    mapped.setVehicleType(vehicleType);
    mapped.setCount(vehicleTypeAvailability.getCount());
    return mapped;
  }

  private List<VehicleDocksAvailability> mapVehicleDocksAvailable(
    GBFSVehicleTypes vehicleTypesFeed,
    List<GBFSVehicleDocksAvailable> vehicleDocksAvailable,
    List<PricingPlan> pricingPlans,
    String language
  ) {
    var mappedVehicleTypes = vehicleTypesFeed
      .getData()
      .getVehicleTypes()
      .stream()
      .map(vehicleType ->
        vehicleTypeMapper.mapVehicleType(vehicleType, pricingPlans, language)
      )
      .collect(Collectors.toMap(VehicleType::getId, vehicleType -> vehicleType));

    return vehicleDocksAvailable
      .stream()
      .map(vehicleDocksAvailability ->
        mapVehicleDocksAvailability(mappedVehicleTypes, vehicleDocksAvailability)
      )
      .collect(Collectors.toList());
  }

  private VehicleDocksAvailability mapVehicleDocksAvailability(
    Map<String, VehicleType> mappedVehicleTypes,
    GBFSVehicleDocksAvailable vehicleDocksAvailability
  ) {
    var vehicleTypes = vehicleDocksAvailability
      .getVehicleTypeIds()
      .stream()
      .map(mappedVehicleTypes::get)
      .toList();

    var mapped = new VehicleDocksAvailability();
    mapped.setVehicleTypes(vehicleTypes);
    mapped.setCount(vehicleDocksAvailability.getCount());
    return mapped;
  }
}
