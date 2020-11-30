package org.entur.lamassu.model.gbfs.v2_1;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class SystemRegions extends GBFSBase {
    @JsonProperty("data") Data data;

    public static class Data {
        @JsonProperty("regions")
        List<Region> regions;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Region {
        @JsonProperty("region_id") String regionId;
        @JsonProperty("name") String name;
    }
}
