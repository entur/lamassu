package org.entur.lamassu.model.gbfs.v2_1;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public class GBFS extends GBFSBase {
    @JsonProperty("data") Map<String, Data> data;

    public Map<String, Data> getData() {
        return data;
    }

    public static class Data {
        @JsonProperty("feeds")
        List<GBFSFeed> feeds;

        public List<GBFSFeed> getFeeds() {
            return feeds;
        }
    }

    public static class GBFSFeed {
        @JsonProperty("name") GBFSFeedName name;
        @JsonProperty("url") String url;

        public GBFSFeedName getName() {
            return name;
        }

        public String getUrl() {
            return url;
        }
    }

}
