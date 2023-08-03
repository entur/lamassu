package org.entur.lamassu.model.entities;

import java.io.Serializable;

public class RentalUris implements Serializable {

  private String android;
  private String ios;
  private String web;

  public String getAndroid() {
    return android;
  }

  public void setAndroid(String android) {
    this.android = android;
  }

  public String getIos() {
    return ios;
  }

  public void setIos(String ios) {
    this.ios = ios;
  }

  public String getWeb() {
    return web;
  }

  public void setWeb(String web) {
    this.web = web;
  }

  @Override
  public String toString() {
    return (
      "RentalUris{" +
      "android='" +
      android +
      '\'' +
      ", ios='" +
      ios +
      '\'' +
      ", web='" +
      web +
      '\'' +
      '}'
    );
  }
}
