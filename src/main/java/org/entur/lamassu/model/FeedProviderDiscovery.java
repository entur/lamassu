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
        @JsonProperty("providers") List<Provider> feedProviders;
        public void setFeedProviders(List<Provider> feedProviders) {
            this.feedProviders = feedProviders;
        }
    }

    public static class Provider {
        @JsonProperty("name") private String name;
        @JsonProperty("url") private String url;

        public void setName(String name) {
            this.name = name;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }
}
