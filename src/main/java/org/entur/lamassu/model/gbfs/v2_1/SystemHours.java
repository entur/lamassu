package org.entur.lamassu.model.gbfs.v2_1;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

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
        MEMBER("member"),
        NONMEMBER("nonmember");

        @JsonValue
        private final String value;

        UserType(String value) {
            this.value = value;
        }
    }

    public enum WeekDay {
        MON("mon"),
        TUE("tue"),
        WED("wed"),
        THU("thu"),
        FRI("fri"),
        SAT("sat"),
        SUN("sun");

        @JsonValue
        private final String value;

        WeekDay(String value) {
            this.value = value;
        }
    }
}
