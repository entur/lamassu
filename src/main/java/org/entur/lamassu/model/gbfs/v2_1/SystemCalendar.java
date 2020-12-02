package org.entur.lamassu.model.gbfs.v2_1;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class SystemCalendar extends GBFSBase {
    @JsonProperty("data") Data data;

    public static class Data {
        @JsonProperty("calendars")
        List<Calendar> calendars;
    }

    public static class Calendar {
        @JsonProperty("start_month") Integer startMonth;
        @JsonProperty("start_day") Integer startDay;
        @JsonProperty("start_year") Integer startYear;
        @JsonProperty("end_month") Integer endMonth;
        @JsonProperty("end_day") Integer endDay;
        @JsonProperty("end_year") Integer endYear;
    }
}
