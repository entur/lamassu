package org.entur.lamassu.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class FeedProviderDiscovery {
    @JsonProperty("last_updated") private Long lastUpdated;
    @JsonProperty("ttl") private Integer ttl;
    @JsonProperty("data") private Data data;

    public void setLastUpdated(Long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public void setTtl(Integer ttl) {
        this.ttl = ttl;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public static class Data {
        @JsonProperty("providers") List<FeedProvider> feedProviders;
        public void setFeedProviders(List<FeedProvider> feedProviders) {
            this.feedProviders = feedProviders;
        }
    }
}
