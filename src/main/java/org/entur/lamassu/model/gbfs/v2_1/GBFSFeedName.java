package org.entur.lamassu.model.gbfs.v2_1;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum GBFSFeedName {
    GBFS,
    GBFS_VERSIONS,
    SYSTEM_INFORMATION,
    VEHICLE_TYPES,
    STATION_INFORMATION,
    STATION_STATUS,
    FREE_BIKE_STATUS,
    SYSTEM_HOURS,
    SYSTEM_CALENDAR,
    SYSTEM_REGIONS,
    SYSTEM_PRICING_PLANS,
    SYSTEM_ALERTS,
    GEOFENCING_ZONES;

    @JsonCreator
    public static GBFSFeedName forValue(String value) {
        return GBFSFeedName.valueOf(value.toUpperCase());
    }

    @JsonValue
    public String toValue() {
        return this.toString().toLowerCase();
    }
}
