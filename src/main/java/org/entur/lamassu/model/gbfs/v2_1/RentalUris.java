package org.entur.lamassu.model.gbfs.v2_1;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class RentalUris {
    @JsonProperty("android") String android;
    @JsonProperty("ios") String ios;
    @JsonProperty("web") String web;
}
