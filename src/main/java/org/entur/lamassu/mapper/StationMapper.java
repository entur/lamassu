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

package org.entur.lamassu.mapper;

import org.entur.lamassu.model.entities.PricingPlan;
import org.entur.lamassu.model.entities.Station;
import org.entur.lamassu.model.entities.System;
import org.entur.lamassu.model.entities.VehicleType;
import org.entur.lamassu.model.entities.VehicleTypeAvailability;
import org.entur.lamassu.model.gbfs.v2_1.StationInformation;
import org.entur.lamassu.model.gbfs.v2_1.StationStatus;
import org.entur.lamassu.model.gbfs.v2_1.VehicleTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class StationMapper {
    private final TranslationMapper translationMapper;
    private final RentalUrisMapper rentalUrisMapper;
    private final VehicleTypeMapper vehicleTypeMapper;

    @Autowired
    public StationMapper(TranslationMapper translationMapper, RentalUrisMapper rentalUrisMapper, VehicleTypeMapper vehicleTypeMapper) {
        this.translationMapper = translationMapper;
        this.rentalUrisMapper = rentalUrisMapper;
        this.vehicleTypeMapper = vehicleTypeMapper;
    }

    public Station mapStation(System system, List<PricingPlan> pricingPlans, StationInformation.Station stationInformation, StationStatus.Station stationStatus, VehicleTypes vehicleTypesFeed, String language) {
        var station = new Station();
        station.setId(stationStatus.getStationId());
        station.setLat(stationInformation.getLat());
        station.setLon(stationInformation.getLon());
        station.setName(translationMapper.mapSingleTranslation(language, stationInformation.getName()));
        station.setAddress(stationInformation.getAddress());
        station.setCapacity(stationInformation.getCapacity());
        station.setRentalUris(rentalUrisMapper.mapRentalUris(stationInformation.getRentalUris()));
        station.setNumBikesAvailable(stationStatus.getNumBikesAvailable());
        station.setVehicleTypesAvailable(mapVehicleTypesAvailable(vehicleTypesFeed, stationStatus.getVehicleTypesAvailable(), language));
        station.setNumDocksAvailable(stationStatus.getNumDocksAvailable());
        station.setInstalled(stationStatus.getInstalled());
        station.setRenting(stationStatus.getRenting());
        station.setReturning(stationStatus.getReturning());
        station.setLastReported(stationStatus.getLastReported());
        station.setSystem(system);
        station.setPricingPlans(pricingPlans);
        return station;
    }

    private List<VehicleTypeAvailability> mapVehicleTypesAvailable(VehicleTypes vehicleTypesFeed, List<StationStatus.VehicleTypeAvailability> vehicleTypesAvailable, String language) {
        if (vehicleTypesAvailable == null) {
            return null;
        }

        var mappedVehicleTypes = vehicleTypesFeed.getData().getVehicleTypes().stream()
                .map(vehicleType -> vehicleTypeMapper.mapVehicleType(vehicleType, language))
                .collect(Collectors.toMap(vehicleType -> vehicleType.getId(), vehicleType -> vehicleType));

        return vehicleTypesAvailable.stream()
                .map(vehicleTypeAvailability -> mapVehicleTypeAvailability(mappedVehicleTypes.get(vehicleTypeAvailability.getVehicleTypeId()), vehicleTypeAvailability, language))
                .collect(Collectors.toList());
    }

    private VehicleTypeAvailability mapVehicleTypeAvailability(VehicleType vehicleType, StationStatus.VehicleTypeAvailability vehicleTypeAvailability, String language) {
        var mapped = new VehicleTypeAvailability();
        mapped.setVehicleType(vehicleType);
        mapped.setCount(vehicleTypeAvailability.getCount());
        return mapped;
    }
}
