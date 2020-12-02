package org.entur.lamassu.model.gbfs.v2_1;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class SystemHours extends GBFSBase {
    @JsonProperty("data") Data data;

    public static class Data {
        @JsonProperty("rental_hours")
        List<RentalHour> rentalHours;
    }

    public static class RentalHour {
        @JsonProperty("user_types") List<UserType> userTypes;
        @JsonProperty("days") List<WeekDay> days;
        @JsonProperty("start_time") String startTime;
        @JsonProperty("end_time") String endTime;
    }

    public enum UserType {
        member,
        nonmember
    }

    public enum WeekDay {
        mon,
        tue,
        wed,
        thu,
        fri,
        sat,
        sun
    }
}
