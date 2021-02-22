package org.entur.lamassu.model.entities;

public class System implements Entity {
    String id;
    String language;
    String name;
    String shortName;
    String operator;
    String url;
    String purchaseUrl;
    String startDate;
    String phoneNumber;
    String email;
    String feedContactEmail;
    String timezone;
    String licenseUrl;
    RentalApps rentalApps;

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPurchaseUrl() {
        return purchaseUrl;
    }

    public void setPurchaseUrl(String purchaseUrl) {
        this.purchaseUrl = purchaseUrl;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFeedContactEmail() {
        return feedContactEmail;
    }

    public void setFeedContactEmail(String feedContactEmail) {
        this.feedContactEmail = feedContactEmail;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getLicenseUrl() {
        return licenseUrl;
    }

    public void setLicenseUrl(String licenseUrl) {
        this.licenseUrl = licenseUrl;
    }

    public RentalApps getRentalApps() {
        return rentalApps;
    }

    public void setRentalApps(RentalApps rentalApps) {
        this.rentalApps = rentalApps;
    }

    @Override
    public String toString() {
        return "System{" +
                "id='" + id + '\'' +
                ", language='" + language + '\'' +
                ", name='" + name + '\'' +
                ", shortName='" + shortName + '\'' +
                ", operator='" + operator + '\'' +
                ", url='" + url + '\'' +
                ", purchaseUrl='" + purchaseUrl + '\'' +
                ", startDate='" + startDate + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", email='" + email + '\'' +
                ", feedContactEmail='" + feedContactEmail + '\'' +
                ", timezone='" + timezone + '\'' +
                ", licenseUrl='" + licenseUrl + '\'' +
                ", rentalApps=" + rentalApps +
                '}';
    }
}
