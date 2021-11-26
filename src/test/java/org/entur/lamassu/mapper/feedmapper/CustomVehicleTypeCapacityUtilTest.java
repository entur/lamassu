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

import org.entur.gbfs.v2_2.station_status.GBFSStation;
import org.entur.gbfs.v2_2.station_status.GBFSStationStatus;
import org.entur.gbfs.v2_2.vehicle_types.GBFSVehicleType;
import org.entur.gbfs.v2_2.vehicle_types.GBFSVehicleTypes;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class CustomVehicleTypeCapacityUtilTest {
    @Test
    void testAddCustomVehicleTypeCapacityToStations() {
        var stationStatus = new GBFSStationStatus()
                .withData(
                        new org.entur.gbfs.v2_2.station_status.GBFSData().withStations(
                                List.of(
                                        new GBFSStation()
                                                .withNumBikesAvailable(2.0)
                                )
                        )
                );
        var vehicleTypes = new GBFSVehicleTypes()
                .withData(
                        new org.entur.gbfs.v2_2.vehicle_types.GBFSData().withVehicleTypes(
                                List.of(
                                        new GBFSVehicleType()
                                                .withVehicleTypeId("TST:VehicleType:1")
                                )
                        )
                );
        CustomVehicleTypeCapacityUtil.addCustomVehicleTypeCapacityToStations(
                stationStatus,
                vehicleTypes
        );

        Assertions.assertEquals(
                2.0,
                stationStatus.getData().getStations().get(0).getVehicleTypesAvailable().get(0).getCount()
        );
    }
}
