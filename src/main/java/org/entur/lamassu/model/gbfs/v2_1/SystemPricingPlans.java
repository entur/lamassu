package org.entur.lamassu.model.gbfs.v2_1;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class SystemPricingPlans extends GBFSBase {
    @JsonProperty("data") Data data;

    public Data getData() {
        return data;
    }

    public static class Data {
        @JsonProperty("plans")
        List<Plan> plans;

        public List<Plan> getPlans() {
            return plans;
        }
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

        public String getPlanId() {
            return planId;
        }

        public String getUrl() {
            return url;
        }

        public String getName() {
            return name;
        }

        public String getCurrency() {
            return currency;
        }


        public Float getPrice() {
            return price;
        }

        public Boolean getTaxable() {
            return isTaxable;
        }

        public String getDescription() {
            return description;
        }

        public List<PricingSegment> getPerKmPricing() {
            return perKmPricing;
        }

        public List<PricingSegment> getPerMinPricing() {
            return perMinPricing;
        }

        public Boolean getSurgePricing() {
            return surgePricing;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PricingSegment {
        @JsonProperty("start") Integer start;
        @JsonProperty("rate") Float rate;
        @JsonProperty("interval") Integer interval;
        @JsonProperty("end") Integer end;

        public Integer getStart() {
            return start;
        }

        public Float getRate() {
            return rate;
        }

        public Integer getInterval() {
            return interval;
        }

        public Integer getEnd() {
            return end;
        }
    }
}
