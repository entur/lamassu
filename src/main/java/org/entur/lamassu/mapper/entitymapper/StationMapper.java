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

import org.entur.gbfs.v2_2.station_information.GBFSStation;
import org.entur.gbfs.v2_2.station_status.GBFSVehicleTypesAvailable;
import org.entur.gbfs.v2_2.vehicle_types.GBFSVehicleTypes;
import org.entur.lamassu.model.entities.PricingPlan;
import org.entur.lamassu.model.entities.Station;
import org.entur.lamassu.model.entities.System;
import org.entur.lamassu.model.entities.VehicleType;
import org.entur.lamassu.model.entities.VehicleTypeAvailability;
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

    public Station mapStation(System system, List<PricingPlan> pricingPlans, GBFSStation stationInformation, org.entur.gbfs.v2_2.station_status.GBFSStation stationStatus, GBFSVehicleTypes vehicleTypesFeed, String language) {
        var station = new Station();
        station.setId(stationStatus.getStationId());
        station.setLat(stationInformation.getLat());
        station.setLon(stationInformation.getLon());
        station.setName(translationMapper.mapSingleTranslation(language, stationInformation.getName()));
        station.setAddress(stationInformation.getAddress());
        station.setCapacity(stationInformation.getCapacity() != null ? stationInformation.getCapacity().intValue() : null);
        station.setRentalUris(rentalUrisMapper.mapRentalUris(stationInformation.getRentalUris()));
        station.setNumBikesAvailable(stationStatus.getNumBikesAvailable() != null ? stationStatus.getNumBikesAvailable().intValue() : null);
        station.setVehicleTypesAvailable(mapVehicleTypesAvailable(vehicleTypesFeed, stationStatus.getVehicleTypesAvailable(), language));
        station.setNumDocksAvailable(stationStatus.getNumDocksAvailable() != null ? stationStatus.getNumDocksAvailable().intValue() : null);
        station.setInstalled(stationStatus.getIsInstalled());
        station.setRenting(stationStatus.getIsRenting());
        station.setReturning(stationStatus.getIsReturning());
        station.setLastReported(stationStatus.getLastReported().longValue());
        station.setSystem(system);
        station.setPricingPlans(pricingPlans);
        return station;
    }

    private List<VehicleTypeAvailability> mapVehicleTypesAvailable(GBFSVehicleTypes vehicleTypesFeed, List<GBFSVehicleTypesAvailable> vehicleTypesAvailable, String language) {
        var mappedVehicleTypes = vehicleTypesFeed.getData().getVehicleTypes().stream()
                .map(vehicleType -> vehicleTypeMapper.mapVehicleType(vehicleType, language))
                .collect(Collectors.toMap(VehicleType::getId, vehicleType -> vehicleType));

        return vehicleTypesAvailable.stream()
                .map(vehicleTypeAvailability -> mapVehicleTypeAvailability(mappedVehicleTypes.get(vehicleTypeAvailability.getVehicleTypeId()), vehicleTypeAvailability))
                .collect(Collectors.toList());
    }

    private VehicleTypeAvailability mapVehicleTypeAvailability(VehicleType vehicleType, GBFSVehicleTypesAvailable vehicleTypeAvailability) {
        var mapped = new VehicleTypeAvailability();
        mapped.setVehicleType(vehicleType);
        mapped.setCount(vehicleTypeAvailability.getCount().intValue());
        return mapped;
    }
}
