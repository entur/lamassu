package org.entur.lamassu.model.gbfs.v2_1;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class SystemAlerts extends GBFSBase {
    @JsonProperty("data") Data data;

    public static class Data {
        @JsonProperty("alerts")
        List<Alert> alerts;
    }

    public static class Alert {
        @JsonProperty("alert_id") String alertId;
        @JsonProperty("type") AlertType type;
        @JsonProperty("times") List<AlertTime> times;
        @JsonProperty("station_ids") List<String> stationIds;
        @JsonProperty("region_ids") List<String> regionIds;
        @JsonProperty("url") String url;
        @JsonProperty("summary") String summary;
        @JsonProperty("description") String description;
        @JsonProperty("last_updated") Long lastUpdated;
    }

    public enum AlertType {
        SYSTEM_CLOSURE,
        STATION_CLOSURE,
        STATION_MOVE,
        OTHER
    }

    public static class AlertTime {
        @JsonProperty("start") Long start;
        @JsonProperty("end") Long end;
    }
}
