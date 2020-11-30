package org.entur.lamassu.model.gbfs.v2_1;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class GBFS extends GBFSBase {
    @JsonProperty("data") Map<String, GBFSData> data;

    public Map<String, GBFSData> getData() {
        return data;
    }
}
