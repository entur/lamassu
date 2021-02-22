package org.entur.lamassu.model.gbfs.v2_1;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SystemInformation extends GBFSBase {
    @JsonProperty("data")
    Data data;

    public Data getData() {
        return data;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Data {
        @JsonProperty("system_id")
        String systemId;
        @JsonProperty("language")
        String language;
        @JsonProperty("name")
        String name;
        @JsonProperty("short_name")
        String shortName;
        @JsonProperty("operator")
        String operator;
        @JsonProperty("url")
        String url;
        @JsonProperty("purchase_url")
        String purchaseUrl;
        @JsonProperty("start_date")
        String startDate;
        @JsonProperty("phone_number")
        String phoneNumber;
        @JsonProperty("email")
        String email;
        @JsonProperty("feed_contact_email")
        String feedContactEmail;
        @JsonProperty("timezone")
        String timezone;
        @JsonProperty("license_url")
        String licenseUrl;
        @JsonProperty("rental_apps")
        RentalApps rentalApps;

        public String getSystemId() {
            return systemId;
        }

        public String getLanguage() {
            return language;
        }

        public String getName() {
            return name;
        }

        public String getShortName() {
            return shortName;
        }

        public String getOperator() {
            return operator;
        }

        public String getUrl() {
            return url;
        }

        public String getPurchaseUrl() {
            return purchaseUrl;
        }

        public String getStartDate() {
            return startDate;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public String getEmail() {
            return email;
        }

        public String getFeedContactEmail() {
            return feedContactEmail;
        }

        public String getTimezone() {
            return timezone;
        }

        public String getLicenseUrl() {
            return licenseUrl;
        }

        public RentalApps getRentalApps() {
            return rentalApps;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class RentalApps {
        @JsonProperty("ios") RentalApp ios;
        @JsonProperty("android") RentalApp android;

        public RentalApp getIos() {
            return ios;
        }

        public RentalApp getAndroid() {
            return android;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class RentalApp {
        @JsonProperty("store_uri") String storeURI;
        @JsonProperty("discovery_uri") String discoveryURI;

        public String getStoreURI() {
            return storeURI;
        }

        public String getDiscoveryURI() {
            return discoveryURI;
        }
    }
}
