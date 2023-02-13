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

package org.entur.lamassu.mapper.feedmapper;

import org.entur.gbfs.v2_3.station_status.GBFSStationStatus;
import org.entur.gbfs.v2_3.station_status.GBFSVehicleTypesAvailable;
import org.entur.gbfs.v2_3.vehicle_types.GBFSVehicleTypes;

import java.util.List;

public class VehicleTypeCapacityProducer {
    private VehicleTypeCapacityProducer() {}

    /*
        Adds vehicle type availability to stations that don't have it according to the requirements,
        on the condition that the system only has 1 vehicle type. The vehicle type availability count is
        then set to the same value as number of available bikes for that station.
     */
    public static void addToStations(GBFSStationStatus stationStatus, GBFSVehicleTypes vehicleTypes) {
        if (vehicleTypes.getData() != null && vehicleTypes.getData().getVehicleTypes() != null && vehicleTypes.getData().getVehicleTypes().size() == 1) {
            var vehicleType = vehicleTypes.getData().getVehicleTypes().get(0);
            stationStatus.getData().getStations().forEach(station -> {
                if (station.getVehicleTypesAvailable() == null || station.getVehicleTypesAvailable().isEmpty()) {
                    station.setVehicleTypesAvailable(List.of(
                            new GBFSVehicleTypesAvailable()
                                    .withVehicleTypeId(vehicleType.getVehicleTypeId())
                                    .withCount(station.getNumBikesAvailable())
                    ));
                }
            });
        }
    }
}
