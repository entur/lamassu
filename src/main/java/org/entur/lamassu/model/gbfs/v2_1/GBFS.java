package org.entur.lamassu.model.gbfs.v2_1;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

public class GBFS extends GBFSBase {
    @JsonProperty("data") Map<String, Data> data;

    public Map<String, Data> getData() {
        return data;
    }

    public void setData(Map<String, Data> data) {
        this.data = data;
    }

    public static class Data {
        @JsonProperty("feeds")
        List<GBFSFeed> feeds;

        public List<GBFSFeed> getFeeds() {
            return feeds;
        }

        public void setFeeds(List<GBFSFeed> feeds) {
            this.feeds = feeds;
        }
    }

    public static class GBFSFeed {
        @JsonProperty("name") GBFSFeedName name;
        @JsonProperty("url") String url;

        public GBFSFeedName getName() {
            return name;
        }

        public void setName(GBFSFeedName name) {
            this.name = name;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

}
