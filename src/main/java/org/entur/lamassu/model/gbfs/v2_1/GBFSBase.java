package org.entur.lamassu.model.gbfs.v2_1;

import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class GBFSBase {
    @JsonProperty("last_updated") Long lastUpdated;
    @JsonProperty("ttl") Integer ttl;
    @JsonProperty("version") String version;
}
