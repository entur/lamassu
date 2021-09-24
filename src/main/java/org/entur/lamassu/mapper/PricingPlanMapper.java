package org.entur.lamassu.mapper;

import org.entur.gbfs.v2_2.system_pricing_plans.GBFSPerKmPricing;
import org.entur.gbfs.v2_2.system_pricing_plans.GBFSPerMinPricing;
import org.entur.gbfs.v2_2.system_pricing_plans.GBFSPlan;
import org.entur.lamassu.model.entities.PricingPlan;
import org.entur.lamassu.model.entities.PricingSegment;
import org.entur.lamassu.model.gbfs.v2_1.SystemPricingPlans;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PricingPlanMapper {

    private final TranslationMapper translationMapper;

    @Autowired
    public PricingPlanMapper(TranslationMapper translationMapper) {
        this.translationMapper = translationMapper;
    }

    public PricingPlan mapPricingPlan(SystemPricingPlans.Plan plan, String language) {
        var mapped = new PricingPlan();
        mapped.setId(plan.getPlanId());
        mapped.setName(translationMapper.mapSingleTranslation(language, plan.getName()));
        mapped.setDescription(translationMapper.mapSingleTranslation(language, plan.getDescription()));
        mapped.setUrl(plan.getUrl());
        mapped.setCurrency(plan.getCurrency());
        mapped.setPrice(plan.getPrice());
        mapped.setTaxable(plan.getTaxable());
        mapped.setSurgePricing(plan.getSurgePricing());
        mapped.setPerKmPricing(mapPricingSegment(plan.getPerKmPricing()));
        mapped.setPerMinPricing(mapPricingSegment(plan.getPerMinPricing()));
        return mapped;
    }

    private List<PricingSegment> mapPricingSegment(List<SystemPricingPlans.PricingSegment> pricingSegments) {
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

    public PricingPlan mapPricingPlan(GBFSPlan plan, String language) {
        var mapped = new PricingPlan();
        mapped.setId(plan.getPlanId());
        mapped.setName(translationMapper.mapSingleTranslation(language, plan.getName()));
        mapped.setDescription(translationMapper.mapSingleTranslation(language, plan.getDescription()));
        mapped.setUrl(plan.getUrl());
        mapped.setCurrency(plan.getCurrency());
        mapped.setPrice(plan.getPrice().floatValue());
        mapped.setTaxable(plan.getIsTaxable());
        mapped.setSurgePricing(plan.getSurgePricing());
        mapped.setPerKmPricing(mapPerKmPricing(plan.getPerKmPricing()));
        mapped.setPerMinPricing(mapPerMinPricing(plan.getPerMinPricing()));
        return mapped;
    }

    private List<PricingSegment> mapPerKmPricing(List<GBFSPerKmPricing> pricingSegments) {
        if (pricingSegments == null) {
            return null;
        }

        return pricingSegments.stream()
                .map(pricingSegment -> {
                    var mapped = new PricingSegment();
                    mapped.setStart(pricingSegment.getStart() != null ? pricingSegment.getStart().intValue() : null);
                    mapped.setRate(pricingSegment.getRate() != null ? pricingSegment.getRate().floatValue() : null);
                    mapped.setInterval(pricingSegment.getInterval() != null ? pricingSegment.getInterval().intValue() : null);
                    mapped.setEnd(pricingSegment.getEnd() != null ? pricingSegment.getEnd().intValue() : null);
                    return mapped;
                })
                .collect(Collectors.toList());
    }

    private List<PricingSegment> mapPerMinPricing(List<GBFSPerMinPricing> pricingSegments) {
        if (pricingSegments == null) {
            return null;
        }

        return pricingSegments.stream()
                .map(pricingSegment -> {
                    var mapped = new PricingSegment();
                    mapped.setStart(pricingSegment.getStart() != null ? pricingSegment.getStart().intValue() : null);
                    mapped.setRate(pricingSegment.getRate() != null ? pricingSegment.getRate().floatValue() : null);
                    mapped.setInterval(pricingSegment.getInterval() != null ? pricingSegment.getInterval().intValue() : null);
                    mapped.setEnd(pricingSegment.getEnd() != null ? pricingSegment.getEnd().intValue() : null);
                    return mapped;
                })
                .collect(Collectors.toList());
    }
}
