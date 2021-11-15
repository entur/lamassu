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

import org.entur.gbfs.v2_2.free_bike_status.GBFSBike;
import org.entur.gbfs.v2_2.system_pricing_plans.GBFSPerMinPricing;
import org.entur.gbfs.v2_2.system_pricing_plans.GBFSPlan;
import org.entur.gbfs.v2_2.vehicle_types.GBFSVehicleType;
import org.entur.lamassu.model.provider.FeedProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

class FreeBikeStatusFeedMapperTest {
    FreeBikeStatusFeedMapper mapper;

    @BeforeEach
    void prepare() {
        mapper = new FreeBikeStatusFeedMapper();
    }

    @Test
    void testMissingCurrentRangeMeters() {
        var feedProvider = getTestProvider();
        var mapped = mapper.mapBike(new GBFSBike(), feedProvider);
        Assertions.assertNotNull(mapped.getCurrentRangeMeters());
    }

    @Test
    void testCustomData() {
        var feedProvider = getTestProvider();
        var vehicleType = new GBFSVehicleType();
        vehicleType.setVehicleTypeId("TestScooter");
        vehicleType.setName("TestScooter");
        vehicleType.setFormFactor(GBFSVehicleType.FormFactor.SCOOTER);
        vehicleType.setPropulsionType(GBFSVehicleType.PropulsionType.ELECTRIC);
        vehicleType.setMaxRangeMeters(1000.0);
        feedProvider.setVehicleTypes(List.of(vehicleType));

        var plan = new GBFSPlan();
        plan.setPlanId("TestPlan");
        plan.setName("TestPlan");
        plan.setPrice(0.0);
        plan.setIsTaxable(false);
        plan.setCurrency("NOK");
        plan.setDescription("Describe your plan");
        var perMinPricing = new GBFSPerMinPricing();
        perMinPricing.setStart(0.0);
        perMinPricing.setInterval(1.0);
        perMinPricing.setRate(5.0);
        plan.setPerMinPricing(List.of(perMinPricing));
        feedProvider.setPricingPlans(List.of(plan));

        var mapped = mapper.mapBike(new GBFSBike(), feedProvider);

        Assertions.assertEquals(
                "TST:VehicleType:TestScooter",
                mapped.getVehicleTypeId()
        );

        Assertions.assertEquals(
                "TST:PricingPlan:TestPlan",
                mapped.getPricingPlanId()
        );
    }

    private FeedProvider getTestProvider() {
        var feedProvider = new FeedProvider();
        feedProvider.setSystemId("testsystem");
        feedProvider.setCodespace("TST");
        feedProvider.setLanguage("en");

        return feedProvider;
    }
}
