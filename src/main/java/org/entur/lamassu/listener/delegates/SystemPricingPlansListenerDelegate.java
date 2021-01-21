package org.entur.lamassu.listener.delegates;

import org.entur.lamassu.cache.PricingPlanCache;
import org.entur.lamassu.listener.CacheEntryListenerDelegate;
import org.entur.lamassu.mapper.PricingPlanMapper;
import org.entur.lamassu.model.gbfs.v2_1.GBFSBase;
import org.entur.lamassu.model.gbfs.v2_1.SystemPricingPlans;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.cache.event.CacheEntryEvent;
import java.util.stream.Collectors;

@Component
public class SystemPricingPlansListenerDelegate implements CacheEntryListenerDelegate<GBFSBase, SystemPricingPlans> {

    private final PricingPlanMapper pricingPlanMapper;
    private final PricingPlanCache pricingPlanCache;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public SystemPricingPlansListenerDelegate(PricingPlanMapper pricingPlanMapper, PricingPlanCache pricingPlanCache) {
        this.pricingPlanMapper = pricingPlanMapper;
        this.pricingPlanCache = pricingPlanCache;
    }

    @Override
    public void onCreated(CacheEntryEvent<? extends String, GBFSBase> event) {
        addOrUpdateSystemPricingPlan(event);
    }

    @Override
    public void onUpdated(CacheEntryEvent<? extends String, GBFSBase> event) {
        addOrUpdateSystemPricingPlan(event);
    }

    @Override
    public void onRemoved(CacheEntryEvent<? extends String, GBFSBase> event) {
        // TODO implement
    }

    @Override
    public void onExpired(CacheEntryEvent<? extends String, GBFSBase> event) {
        // TODO implement
    }

    private void addOrUpdateSystemPricingPlan(CacheEntryEvent<? extends String, ? extends GBFSBase> event) {
        var pricingPlansFeed = (SystemPricingPlans) event.getValue();
        try {
            var pricingPlans = pricingPlansFeed.getData().getPlans().stream()
                    .map(pricingPlanMapper::mapPricingPlan).collect(Collectors.toList());
            pricingPlanCache.updateAll(pricingPlans);
            logger.info("Added pricing plans to pricing plan cache from feed {}", event.getKey());
        } catch (NullPointerException e) {
            logger.warn("Caught NullPointerException when updating pricing plan cache from pricingPlansFeed: {}", pricingPlansFeed, e);
        }
    }
}
