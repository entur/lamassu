package org.entur.lamassu.model.gbfs.v2_1;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class MultiPolygon {
    @JsonProperty("type") String type = "MultiPolygon";
    @JsonProperty("coordinates") List<List<List<List<Double>>>> coordinates;

    public String getType() {
        return type;
    }

    public List<List<List<List<Double>>>> getCoordinates() {
        return coordinates;
    }
}
