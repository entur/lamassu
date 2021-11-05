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

import org.entur.gbfs.v2_2.system_pricing_plans.GBFSData;
import org.entur.gbfs.v2_2.system_pricing_plans.GBFSPlan;
import org.entur.gbfs.v2_2.system_pricing_plans.GBFSSystemPricingPlans;
import org.entur.lamassu.model.provider.FeedProvider;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static org.entur.lamassu.mapper.feedmapper.IdMappers.PRICING_PLAN_ID_TYPE;

@Component
public class SystemPricingPlansFeedMapper implements FeedMapper<GBFSSystemPricingPlans> {
    @Override
    public GBFSSystemPricingPlans map(GBFSSystemPricingPlans source, FeedProvider feedProvider) {
        var mapped = new GBFSSystemPricingPlans();
        mapped.setVersion(source.getVersion());
        mapped.setTtl(source.getTtl());
        mapped.setLastUpdated(source.getLastUpdated());
        mapped.setData(mapData(source.getData(), feedProvider.getCodespace()));
        return mapped;
    }

    private GBFSData mapData(GBFSData data, String codespace) {
        var mapped = new GBFSData();
        mapped.setPlans(mapPlans(data.getPlans(), codespace));
        return mapped;
    }

    private List<GBFSPlan> mapPlans(List<GBFSPlan> plans, String codespace) {
        return plans.stream()
                .map(plan -> mapPlan(plan, codespace))
                .collect(Collectors.toList());
    }

    private GBFSPlan mapPlan(GBFSPlan plan, String codespace) {
        var mapped = new GBFSPlan();
        mapped.setPlanId(IdMappers.mapId(codespace, PRICING_PLAN_ID_TYPE, plan.getPlanId()));
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
