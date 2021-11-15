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

package org.entur.lamassu.mapper.feedmapper;

import org.entur.gbfs.v2_2.vehicle_types.GBFSVehicleType;
import org.entur.lamassu.model.provider.FeedProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class VehicleTypesFeedMapperTest {

    @Test
    void testCustomVehicleTypes() {
        var mapper = new VehicleTypesFeedMapper("2.2");
        var feed = mapper.map(null, getTestProvider());
        Assertions.assertEquals("TST:VehicleType:TestScooter", feed.getData().getVehicleTypes().get(0).getVehicleTypeId());
    }

    private FeedProvider getTestProvider() {
        var feedProvider = new FeedProvider();
        feedProvider.setSystemId("testsystem");
        feedProvider.setCodespace("TST");
        var vehicleType = new GBFSVehicleType();
        vehicleType.setVehicleTypeId("TestScooter");
        vehicleType.setName("TestScooter");
        vehicleType.setFormFactor(GBFSVehicleType.FormFactor.SCOOTER);
        vehicleType.setPropulsionType(GBFSVehicleType.PropulsionType.ELECTRIC);
        vehicleType.setMaxRangeMeters(1000.0);
        feedProvider.setVehicleTypes(List.of(vehicleType));
        return feedProvider;

    }
}
