package org.entur.lamassu.model.discovery;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class SystemDiscovery {

  @JsonProperty("systems")
  List<System> systems;

  public void setSystems(List<System> systems) {
    this.systems = systems;
  }

  public List<System> getSystems() {
    return systems;
  }
}
