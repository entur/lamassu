package org.entur.lamassu.model.gbfs.v2_1;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class SystemPricingPlans extends GBFSBase {
    @JsonProperty("data") Data data;

    public static class Data {
        @JsonProperty("plans")
        List<Plan> plans;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Plan {
        @JsonProperty("plan_id") String planId;
        @JsonProperty("url") String url;
        @JsonProperty("name") String name;
        @JsonProperty("currency") String currency;
        @JsonProperty("price") Float price;
        @JsonProperty("is_taxable") Boolean isTaxable;
        @JsonProperty("description") String description;
        @JsonProperty("per_km_pricing") List<PricingSegment> perKmPricing;
        @JsonProperty("per_min_pricing") List<PricingSegment> perMinPricing;
        @JsonProperty("surge_pricing") Boolean surgePricing;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PricingSegment {
        @JsonProperty("start") Integer start;
        @JsonProperty("rate") Float rate;
        @JsonProperty("interval") Integer interval;
        @JsonProperty("end") Integer end;
    }
}
