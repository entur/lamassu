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
import org.entur.lamassu.model.entities.TranslatedString;
import org.entur.lamassu.model.entities.Translation;
import org.entur.lamassu.model.gbfs.v2_1.StationInformation;
import org.entur.lamassu.model.gbfs.v2_1.StationStatus;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StationMapper {

    public Station mapStation(System system, List<PricingPlan> pricingPlans, StationInformation.Station stationInformation, StationStatus.Station stationStatus, String language) {
        var station = new Station();
        station.setId(stationStatus.getStationId());
        station.setLat(stationInformation.getLat());
        station.setLon(stationInformation.getLon());
        station.setName(mapTranslation(stationInformation.getName(), language));
        station.setAddress(stationInformation.getAddress());
        station.setCapacity(stationInformation.getCapacity());
        station.setNumBikesAvailable(stationStatus.getNumBikesAvailable());
        station.setNumDocksAvailable(stationStatus.getNumDocksAvailable());
        station.setInstalled(stationStatus.getInstalled());
        station.setRenting(stationStatus.getRenting());
        station.setReturning(stationStatus.getReturning());
        station.setLastReported(stationStatus.getLastReported());
        station.setSystem(system);
        station.setPricingPlans(pricingPlans);
        return station;
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
