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

package org.entur.lamassu.leader.entityupdater;

import java.time.Instant;
import java.util.concurrent.TimeUnit;
import org.entur.lamassu.cache.EntityCache;
import org.entur.lamassu.mapper.entitymapper.PricingPlanMapper;
import org.entur.lamassu.model.entities.PricingPlan;
import org.entur.lamassu.util.CacheUtil;
import org.mobilitydata.gbfs.v3_0.system_pricing_plans.GBFSSystemPricingPlans;
import org.springframework.stereotype.Component;

@Component
public class PricingPlansUpdater {

  private final EntityCache<PricingPlan> pricingPlanCache;
  private final PricingPlanMapper pricingPlanMapper;

  public PricingPlansUpdater(
    EntityCache<PricingPlan> pricingPlanCache,
    PricingPlanMapper pricingPlanMapper
  ) {
    this.pricingPlanCache = pricingPlanCache;
    this.pricingPlanMapper = pricingPlanMapper;
  }

  public void update(GBFSSystemPricingPlans gbfsSystemPricingPlans) {
    var mapped = gbfsSystemPricingPlans
      .getData()
      .getPlans()
      .stream()
      .map(pricingPlanMapper::mapPricingPlan)
      .collect(EntityCollectors.toMapWithDuplicateWarning(PricingPlan.class));

    var lastUpdated = gbfsSystemPricingPlans.getLastUpdated();
    var ttl = gbfsSystemPricingPlans.getTtl();

    pricingPlanCache.updateAll(
      mapped,
      CacheUtil.getTtl(
        (int) Instant.now().getEpochSecond(),
        (int) (lastUpdated.getTime() / 100),
        ttl,
        86400
      ),
      TimeUnit.SECONDS
    );
  }
}
