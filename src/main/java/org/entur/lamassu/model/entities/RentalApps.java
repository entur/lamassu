package org.entur.lamassu.model.entities;

public class RentalApps {
    private RentalApp ios;
    private RentalApp android;

    public RentalApp getIos() {
        return ios;
    }

    public void setIos(RentalApp ios) {
        this.ios = ios;
    }

    public RentalApp getAndroid() {
        return android;
    }

    public void setAndroid(RentalApp android) {
        this.android = android;
    }

    @Override
    public String toString() {
        return "RentalApps{" +
                "ios=" + ios +
                ", android=" + android +
                '}';
    }
}
