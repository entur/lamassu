package org.entur.lamassu.service;

public class RangeQueryParameters {

  private Double lat;
  private Double lon;
  private Double range;

  public RangeQueryParameters() {}

  public RangeQueryParameters(Double lat, Double lon, Double range) {
    this.lat = lat;
    this.lon = lon;
    this.range = range;
  }

  public Double getLat() {
    return lat;
  }

  public void setLat(Double lat) {
    this.lat = lat;
  }

  public Double getLon() {
    return lon;
  }

  public void setLon(Double lon) {
    this.lon = lon;
  }

  public Double getRange() {
    return range;
  }

  public void setRange(Double range) {
    this.range = range;
  }
}
