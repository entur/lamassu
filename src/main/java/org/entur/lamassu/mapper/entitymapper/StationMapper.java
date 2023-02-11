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

import org.entur.gbfs.v2_3.station_information.GBFSStation;
import org.entur.gbfs.v2_3.station_information.GBFSStationArea;
import org.entur.gbfs.v2_3.station_information.GBFSVehicleCapacity;
import org.entur.gbfs.v2_3.station_information.GBFSVehicleTypeCapacity;
import org.entur.gbfs.v2_3.station_status.GBFSVehicleDocksAvailable;
import org.entur.gbfs.v2_3.station_status.GBFSVehicleTypesAvailable;
import org.entur.gbfs.v2_3.system_regions.GBFSSystemRegions;
import org.entur.gbfs.v2_3.vehicle_types.GBFSVehicleTypes;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class StationMapper {
    private final TranslationMapper translationMapper;
    private final RentalUrisMapper rentalUrisMapper;
    private final VehicleTypeMapper vehicleTypeMapper;

    private static final Logger logger = LoggerFactory.getLogger(StationMapper.class);

    @Autowired
    public StationMapper(TranslationMapper translationMapper, RentalUrisMapper rentalUrisMapper, VehicleTypeMapper vehicleTypeMapper) {
        this.translationMapper = translationMapper;
        this.rentalUrisMapper = rentalUrisMapper;
        this.vehicleTypeMapper = vehicleTypeMapper;
    }

    public Station mapStation(System system, List<PricingPlan> pricingPlans, GBFSStation stationInformation, org.entur.gbfs.v2_3.station_status.GBFSStation stationStatus, GBFSVehicleTypes vehicleTypesFeed, GBFSSystemRegions regions, String language) {
        var station = new Station();
        station.setId(stationStatus.getStationId());
        station.setName(translationMapper.mapSingleTranslation(language, stationInformation.getName()));
        station.setShortName(translationMapper.mapSingleTranslation(language, stationInformation.getShortName()));
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
        station.setCapacity(stationInformation.getCapacity() != null ? stationInformation.getCapacity().intValue() : null);
        station.setVehicleCapacity(stationInformation.getVehicleCapacity() != null ? mapVehicleCapacities(stationInformation.getVehicleCapacity(), mapVehicleTypes(vehicleTypesFeed, pricingPlans, language)) : null);
        station.setVehicleTypeCapacity(stationInformation.getVehicleTypeCapacity() != null ? mapVehicleTypeCapacities(stationInformation.getVehicleTypeCapacity(), mapVehicleTypes(vehicleTypesFeed, pricingPlans, language)) : null);
        station.setValetStation(stationInformation.getIsValetStation());
        station.setChargingStation(stationInformation.getIsChargingStation());
        station.setRentalUris(rentalUrisMapper.mapRentalUris(stationInformation.getRentalUris()));
        station.setNumBikesAvailable(stationStatus.getNumBikesAvailable() != null ? stationStatus.getNumBikesAvailable().intValue() : null);
        station.setVehicleTypesAvailable(stationStatus.getVehicleTypesAvailable() != null ? mapVehicleTypesAvailable(vehicleTypesFeed, stationStatus.getVehicleTypesAvailable(), pricingPlans, language) : null);
        station.setNumBikesDisabled(stationStatus.getNumBikesDisabled() != null ? stationStatus.getNumBikesDisabled().intValue() : null);
        station.setNumDocksAvailable(stationStatus.getNumDocksAvailable() != null ? stationStatus.getNumDocksAvailable().intValue() : null);
        station.setVehicleDocksAvailable(stationStatus.getVehicleDocksAvailable() != null ? mapVehicleDocksAvailable(vehicleTypesFeed, stationStatus.getVehicleDocksAvailable(), pricingPlans, language) : null);
        station.setNumDocksDisabled(station.getNumDocksDisabled());
        station.setInstalled(stationStatus.getIsInstalled());
        station.setRenting(stationStatus.getIsRenting());
        station.setReturning(stationStatus.getIsReturning());
        station.setLastReported(stationStatus.getLastReported() != null ? stationStatus.getLastReported().longValue() : null);
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

    private MultiPolygon mapStationArea(GBFSStationArea stationArea) {
        if (stationArea == null) {
            return null;
        }

        var multiPolygon = new MultiPolygon();
        multiPolygon.setCoordinates(stationArea.getCoordinates());
        return multiPolygon;
    }

    private List<RentalMethod> mapRentalMethods(List<org.entur.gbfs.v2_3.station_information.RentalMethod> rentalMethods) {
        return Optional.ofNullable(rentalMethods)
                .map(values -> values.stream()
                        .filter(Objects::nonNull)
                        .map(rentalMethod -> RentalMethod.valueOf(rentalMethod.name().toUpperCase()))
                        .collect(Collectors.toList())
                ).orElse(null);
    }

    private Region mapRegion(GBFSSystemRegions regions, String regionId, String language) {
        if (regionId == null) {
            return null;
        }

        var sourceRegion = Optional.ofNullable(regions)
                .flatMap(r -> r.getData().getRegions().stream()
                        .filter(region -> region.getRegionId().equals(regionId))
                        .findFirst()
                );

        if (sourceRegion.isPresent()) {
            var region = new Region();
            region.setId(regionId);
            region.setName(translationMapper.mapSingleTranslation(language, sourceRegion.get().getName()));
            return region;
        } else {
            logger.warn("Could not map regionId to a region from system_regions feed {}", regionId);
            return null;
        }
    }

    private List<VehicleTypeCapacity> mapVehicleCapacities(GBFSVehicleCapacity vehicleCapacity, Map<String, VehicleType> vehicleTypes) {
        return vehicleCapacity.getAdditionalProperties().entrySet().stream()
                .map(entry -> {
                    var mapped = new VehicleTypeCapacity();
                    mapped.setVehicleType(vehicleTypes.get(entry.getKey()));
                    mapped.setCount(entry.getValue().intValue());
                    return mapped;
                }).collect(Collectors.toList());
    }

    private List<VehicleTypeCapacity> mapVehicleTypeCapacities(GBFSVehicleTypeCapacity vehicleCapacity, Map<String, VehicleType> vehicleTypes) {
        return vehicleCapacity.getAdditionalProperties().entrySet().stream()
                .map(entry -> mapVehicleTypeCapacityFromMapEntry(entry, vehicleTypes))
                .collect(Collectors.toList());
    }

    private VehicleTypeCapacity mapVehicleTypeCapacityFromMapEntry(Map.Entry<String, Double> entry, Map<String, VehicleType> vehicleTypes) {
        var mapped = new VehicleTypeCapacity();
        mapped.setVehicleType(vehicleTypes.get(entry.getKey()));
        mapped.setCount(entry.getValue().intValue());
        return mapped;
    }

    private Map<String, VehicleType> mapVehicleTypes(GBFSVehicleTypes vehicleTypesFeed, List<PricingPlan> pricingPlans, String language) {
        return vehicleTypesFeed.getData().getVehicleTypes().stream()
                .map(vehicleType -> vehicleTypeMapper.mapVehicleType(vehicleType, pricingPlans, language))
                .collect(Collectors.toMap(VehicleType::getId, vehicleType -> vehicleType));
    }

    private List<VehicleTypeAvailability> mapVehicleTypesAvailable(GBFSVehicleTypes vehicleTypesFeed, List<GBFSVehicleTypesAvailable> vehicleTypesAvailable, List<PricingPlan> pricingPlans, String language) {
        var mappedVehicleTypes = vehicleTypesFeed.getData().getVehicleTypes().stream()
                .map(vehicleType -> vehicleTypeMapper.mapVehicleType(vehicleType, pricingPlans, language))
                .collect(Collectors.toMap(VehicleType::getId, vehicleType -> vehicleType));

        return vehicleTypesAvailable.stream()
                .map(vehicleTypeAvailability -> mapVehicleTypeAvailability(mappedVehicleTypes.get(vehicleTypeAvailability.getVehicleTypeId()), vehicleTypeAvailability))
                .collect(Collectors.toList());
    }

    private VehicleTypeAvailability mapVehicleTypeAvailability(VehicleType vehicleType, GBFSVehicleTypesAvailable vehicleTypeAvailability) {
        var mapped = new VehicleTypeAvailability();
        mapped.setVehicleType(vehicleType);
        mapped.setCount(vehicleTypeAvailability.getCount());
        return mapped;
    }

    private List<VehicleDocksAvailability> mapVehicleDocksAvailable(GBFSVehicleTypes vehicleTypesFeed, List<GBFSVehicleDocksAvailable> vehicleDocksAvailable, List<PricingPlan> pricingPlans, String language) {
        var mappedVehicleTypes = vehicleTypesFeed.getData().getVehicleTypes().stream()
                .map(vehicleType -> vehicleTypeMapper.mapVehicleType(vehicleType, pricingPlans, language))
                .collect(Collectors.toMap(VehicleType::getId, vehicleType -> vehicleType));

        return vehicleDocksAvailable.stream()
                .map(vehicleDocksAvailability -> mapVehicleDocksAvailability(mappedVehicleTypes, vehicleDocksAvailability))
                .collect(Collectors.toList());
    }

    private VehicleDocksAvailability mapVehicleDocksAvailability(Map<String, VehicleType> mappedVehicleTypes, GBFSVehicleDocksAvailable vehicleDocksAvailability) {
        var vehicleTypes = vehicleDocksAvailability.getVehicleTypeIds().stream()
                .map(mappedVehicleTypes::get)
                .collect(Collectors.toList());

        var mapped = new VehicleDocksAvailability();
        mapped.setVehicleTypes(vehicleTypes);
        mapped.setCount(vehicleDocksAvailability.getCount());
        return mapped;
    }
}
