package org.entur.lamassu.mapper;

import org.entur.lamassu.model.entities.PricingPlan;
import org.entur.lamassu.model.entities.PricingSegment;
import org.entur.lamassu.model.entities.TranslatedString;
import org.entur.lamassu.model.entities.Translation;
import org.entur.lamassu.model.gbfs.v2_1.SystemPricingPlans;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PricingPlanMapper {
    public PricingPlan mapPricingPlan(SystemPricingPlans.Plan plan, String language) {
        var mapped = new PricingPlan();
        mapped.setId(plan.getPlanId());
        mapped.setName(mapTranslation(plan.getName(), language));
        mapped.setDescription(mapTranslation(plan.getDescription(), language));
        mapped.setUrl(plan.getUrl());
        mapped.setCurrency(plan.getCurrency());
        mapped.setPrice(plan.getPrice());
        mapped.setTaxable(plan.getTaxable());
        mapped.setSurgePricing(plan.getSurgePricing());
        mapped.setPerKmPricing(mapPricingSegments(plan.getPerKmPricing()));
        mapped.setPerMinPricing(mapPricingSegments(plan.getPerMinPricing()));
        return mapped;
    }

    private Translation mapTranslation(String value, String language) {
        var translation = new Translation();
        var translatedString = new TranslatedString();
        translatedString.setLanguage(language);
        translatedString.setValue(value);
        translation.setTranslation(List.of(translatedString));
        return translation;
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
