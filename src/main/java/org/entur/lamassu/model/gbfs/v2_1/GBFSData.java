package org.entur.lamassu.model.gbfs.v2_1;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class GBFSData {
    @JsonProperty("feeds") List<GBFSFeed> feeds;

    public List<GBFSFeed> getFeeds() {
        return feeds;
    }
}
