package org.entur.lamassu.model.gbfs.v2_1;

import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class GBFSBase {
    @JsonProperty("last_updated") Long lastUpdated;
    @JsonProperty("ttl") Integer ttl;
    @JsonProperty("version") String version;

    public Long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public Integer getTtl() {
        return ttl;
    }

    public void setTtl(Integer ttl) {
        this.ttl = ttl;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
