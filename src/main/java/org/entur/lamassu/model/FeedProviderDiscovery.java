package org.entur.lamassu.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class FeedProviderDiscovery {
    @JsonProperty("operators") List<Provider> feedProviders;
    public void setFeedProviders(List<Provider> feedProviders) {
        this.feedProviders = feedProviders;
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
