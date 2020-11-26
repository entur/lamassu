package org.entur.lamassu.model.gbfs.v2_1;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GBFSFeed {
    @JsonProperty("name") GBFSFeedName name;
    @JsonProperty("url") String url;
}
