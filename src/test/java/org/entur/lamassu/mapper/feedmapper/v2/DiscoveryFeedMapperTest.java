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

package org.entur.lamassu.mapper.feedmapper.v2;

import java.net.URI;
import java.util.List;
import java.util.Map;
import org.entur.lamassu.model.provider.FeedProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mobilitydata.gbfs.v2_3.gbfs.GBFS;
import org.mobilitydata.gbfs.v2_3.gbfs.GBFSFeed;
import org.mobilitydata.gbfs.v2_3.gbfs.GBFSFeedName;
import org.mobilitydata.gbfs.v2_3.gbfs.GBFSFeeds;
import org.mobilitydata.gbfs.v2_3.system_pricing_plans.GBFSPerMinPricing;
import org.mobilitydata.gbfs.v2_3.system_pricing_plans.GBFSPlan;
import org.mobilitydata.gbfs.v2_3.vehicle_types.GBFSVehicleType;
import org.springframework.test.util.ReflectionTestUtils;

class DiscoveryFeedMapperTest {

  DiscoveryFeedMapper mapper;

  @BeforeEach
  void prepare() {
    mapper = new DiscoveryFeedMapper();
    ReflectionTestUtils.setField(mapper, "targetGbfsVersion", "2.2");
  }

  @Test
  void testMapFeedWhenSourceFeedsDataIsNullReturnsNull() {
    var gbfs = new GBFS();
    gbfs.setFeedsData(null);
    Assertions.assertNull(mapper.map(gbfs, getTestProvider()));
  }

  @Test
  void testMapFeedWithCustomData() {
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
    perMinPricing.setStart(0);
    perMinPricing.setInterval(1);
    perMinPricing.setRate(5.0);
    plan.setPerMinPricing(List.of(perMinPricing));
    feedProvider.setPricingPlans(List.of(plan));

    var gbfs = new GBFS();
    var feeds = new GBFSFeeds();
    var feed = new GBFSFeed();
    feed.setName(GBFSFeedName.GBFS);
    feed.setUrl(URI.create("http://test.com/gbfs"));
    feeds.setFeeds(List.of(feed));

    gbfs.setFeedsData(Map.of("en", feeds));

    var mapped = mapper.map(gbfs, feedProvider);

    Assertions.assertTrue(
      mapped
        .getFeedsData()
        .get("en")
        .getFeeds()
        .stream()
        .anyMatch(f -> f.getName().equals(GBFSFeedName.VehicleTypes))
    );

    Assertions.assertTrue(
      mapped
        .getFeedsData()
        .get("en")
        .getFeeds()
        .stream()
        .anyMatch(f -> f.getName().equals(GBFSFeedName.SystemPricingPlans))
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
