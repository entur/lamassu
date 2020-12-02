package org.entur.lamassu.model.gbfs.v2_1;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class GBFSVersions extends GBFSBase {
    @JsonProperty("data") Data data;

    public static class Data {
        @JsonProperty("versions")
        List<VersionDetail> versions;
    }

    public static class VersionDetail {
        @JsonProperty("version") String version;
        @JsonProperty("url") String url;
    }
}
