package org.entur.lamassu.model.entities;

import java.io.Serializable;

public class RentalApp implements Serializable {

  private String storeUri;
  private String discoveryUri;

  public String getStoreUri() {
    return storeUri;
  }

  public void setStoreUri(String storeUri) {
    this.storeUri = storeUri;
  }

  public String getDiscoveryUri() {
    return discoveryUri;
  }

  public void setDiscoveryUri(String discoveryUri) {
    this.discoveryUri = discoveryUri;
  }

  @Override
  public String toString() {
    return (
      "RentalApp{" +
      "storeURI='" +
      storeUri +
      '\'' +
      ", discoveryURI='" +
      discoveryUri +
      '\'' +
      '}'
    );
  }
}
