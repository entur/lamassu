package org.entur.lamassu.mapper;

import org.entur.lamassu.model.entities.PricingPlan;
import org.entur.lamassu.model.entities.PricingSegment;
import org.entur.lamassu.model.gbfs.v2_1.SystemPricingPlans;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PricingPlanMapper {
    public PricingPlan mapPricingPlan(SystemPricingPlans.Plan plan) {
        var mapped = new PricingPlan();
        mapped.setId(plan.getPlanId());
        mapped.setName(plan.getName());
        mapped.setDescription(plan.getDescription());
        mapped.setUrl(plan.getUrl());
        mapped.setCurrency(plan.getCurrency());
        mapped.setPrice(plan.getPrice());
        mapped.setTaxable(plan.getTaxable());
        mapped.setSurgePricing(plan.getSurgePricing());
        mapped.setPerKmPricing(mapPricingSegments(plan.getPerKmPricing()));
        mapped.setPerMinPricing(mapPricingSegments(plan.getPerMinPricing()));
        return mapped;
    }

    private List<PricingSegment> mapPricingSegments(List<SystemPricingPlans.PricingSegment> pricingSegments) {
        if (pricingSegments == null) {
            return null;
        }

        return pricingSegments.stream()
                .map(pricingSegment -> {
                    var mapped = new PricingSegment();
                    mapped.setStart(pricingSegment.getStart());
                    mapped.setRate(pricingSegment.getRate());
                    mapped.setInterval(pricingSegment.getInterval());
                    mapped.setEnd(pricingSegment.getEnd());
                    return mapped;
                })
                .collect(Collectors.toList());
    }
}
