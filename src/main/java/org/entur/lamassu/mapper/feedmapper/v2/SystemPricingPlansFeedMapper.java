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

package org.entur.lamassu.mapper.feedmapper.v2;

import static org.entur.lamassu.mapper.feedmapper.IdMappers.PRICING_PLAN_ID_TYPE;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import org.entur.gbfs.v2_3.system_pricing_plans.GBFSData;
import org.entur.gbfs.v2_3.system_pricing_plans.GBFSPlan;
import org.entur.gbfs.v2_3.system_pricing_plans.GBFSSystemPricingPlans;
import org.entur.lamassu.mapper.feedmapper.AbstractFeedMapper;
import org.entur.lamassu.mapper.feedmapper.IdMappers;
import org.entur.lamassu.model.provider.FeedProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SystemPricingPlansFeedMapper
  extends AbstractFeedMapper<GBFSSystemPricingPlans> {

  @Value("${org.entur.lamassu.targetGbfsVersion:2.2}")
  private String targetGbfsVersion;

  @Override
  public GBFSSystemPricingPlans map(
    GBFSSystemPricingPlans source,
    FeedProvider feedProvider
  ) {
    if (feedProvider.getPricingPlans() != null) {
      return customPricingPlans(feedProvider);
    }

    if (
      source == null || source.getData() == null || source.getData().getPlans() == null
    ) {
      return null;
    }

    var mapped = new GBFSSystemPricingPlans();
    mapped.setVersion(targetGbfsVersion);
    mapped.setTtl(source.getTtl());
    mapped.setLastUpdated(source.getLastUpdated());
    mapped.setData(mapData(source.getData(), feedProvider));
    return mapped;
  }

  private GBFSSystemPricingPlans customPricingPlans(FeedProvider feedProvider) {
    var custom = new GBFSSystemPricingPlans();
    custom.setVersion(targetGbfsVersion);
    custom.setLastUpdated((int) Instant.now().getEpochSecond());
    custom.setTtl((int) Duration.ofMinutes(5).toSeconds());
    var data = new GBFSData();
    data.setPlans(feedProvider.getPricingPlans());
    custom.setData(mapData(data, feedProvider));
    return custom;
  }

  private GBFSData mapData(GBFSData data, FeedProvider feedProvider) {
    var mapped = new GBFSData();
    var plans = mapPlans(data.getPlans(), feedProvider);
    mapped.setPlans(plans);
    return mapped;
  }

  private List<GBFSPlan> mapPlans(List<GBFSPlan> plans, FeedProvider feedProvider) {
    return plans
      .stream()
      .map(plan -> mapPlan(plan, feedProvider))
      .collect(Collectors.toList());
  }

  private GBFSPlan mapPlan(GBFSPlan plan, FeedProvider feedProvider) {
    var mapped = new GBFSPlan();
    mapped.setPlanId(
      IdMappers.mapId(feedProvider.getCodespace(), PRICING_PLAN_ID_TYPE, plan.getPlanId())
    );
    mapped.setName(plan.getName());
    mapped.setDescription(plan.getDescription());
    mapped.setCurrency(plan.getCurrency());
    mapped.setIsTaxable(plan.getIsTaxable());
    mapped.setPrice(plan.getPrice());
    mapped.setUrl(plan.getUrl());
    mapped.setSurgePricing(plan.getSurgePricing());
    mapped.setPerKmPricing(plan.getPerKmPricing());
    mapped.setPerMinPricing(plan.getPerMinPricing());
    return mapped;
  }
}
