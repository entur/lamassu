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

import java.util.List;
import org.entur.gbfs.v2_3.system_pricing_plans.GBFSPerMinPricing;
import org.entur.gbfs.v2_3.system_pricing_plans.GBFSPlan;
import org.entur.gbfs.v2_3.vehicle_types.GBFSVehicleType;
import org.entur.lamassu.model.provider.FeedProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class SystemPricingPlansFeedMapperTest {

  @Test
  void testCustomPricingPlans() {
    var mapper = new SystemPricingPlansFeedMapper();
    ReflectionTestUtils.setField(mapper, "targetGbfsVersion", "2.2");
    var feed = mapper.map(null, getTestProvider());
    Assertions.assertEquals(
      "TST:PricingPlan:TestPlan",
      feed.getData().getPlans().get(0).getPlanId()
    );
  }

  private FeedProvider getTestProvider() {
    var feedProvider = new FeedProvider();
    feedProvider.setSystemId("testsystem");
    feedProvider.setCodespace("TST");
    var plan = new GBFSPlan();
    plan.setPlanId("TestPlan");
    plan.setName("TestPlan");
    plan.setPrice(0.0);
    plan.setIsTaxable(false);
    plan.setCurrency("NOK");
    plan.setDescription("Describe your plan");
    var perMinPricing = new GBFSPerMinPricing();
    perMinPricing.setStart(0);
    perMinPricing.setInterval(1);
    perMinPricing.setRate(5.0);
    plan.setPerMinPricing(List.of(perMinPricing));
    feedProvider.setPricingPlans(List.of(plan));
    return feedProvider;
  }
}
