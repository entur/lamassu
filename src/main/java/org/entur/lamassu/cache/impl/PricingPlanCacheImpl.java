package org.entur.lamassu.cache.impl;

import org.entur.lamassu.cache.PricingPlanCache;
import org.entur.lamassu.model.PricingPlan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.cache.Cache;

@Component
public class PricingPlanCacheImpl extends EntityCacheImpl<PricingPlan> implements PricingPlanCache {
    protected PricingPlanCacheImpl(@Autowired Cache<String, PricingPlan> cache) {
        super(cache);
    }
}
