/*
 *
 *
 *  * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 *  * the European Commission - subsequent versions of the EUPL (the "Licence");
 *  * You may not use this work except in compliance with the Licence.
 *  * You may obtain a copy of the Licence at:
 *  *
 *  *   https://joinup.ec.europa.eu/software/page/eupl
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the Licence is distributed on an "AS IS" basis,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the Licence for the specific language governing permissions and
 *  * limitations under the Licence.
 *
 */

package org.entur.lamassu.model.entities;

import java.io.Serializable;
import java.util.List;

public class GeofencingZonesData implements Entity {

  private String systemId;
  private List<GeofencingZone> zones;
  private List<GeofencingZones.Rule> globalRules;

  public String getSystemId() {
    return systemId;
  }

  public void setSystemId(String systemId) {
    this.systemId = systemId;
  }

  public List<GeofencingZone> getZones() {
    return zones;
  }

  public void setZones(List<GeofencingZone> zones) {
    this.zones = zones;
  }

  public List<GeofencingZones.Rule> getGlobalRules() {
    return globalRules;
  }

  public void setGlobalRules(List<GeofencingZones.Rule> globalRules) {
    this.globalRules = globalRules;
  }

  @Override
  public String getId() {
    return getSystemId();
  }

  public static class GeofencingZone implements Serializable {

    private String name;
    private Long start;
    private Long end;
    private List<GeofencingZones.Rule> rules;
    private List<List<String>> polylineEncodedMultiPolygon;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public Long getStart() {
      return start;
    }

    public void setStart(Long start) {
      this.start = start;
    }

    public Long getEnd() {
      return end;
    }

    public void setEnd(Long end) {
      this.end = end;
    }

    public List<GeofencingZones.Rule> getRules() {
      return rules;
    }

    public void setRules(List<GeofencingZones.Rule> rules) {
      this.rules = rules;
    }

    public List<List<String>> getPolylineEncodedMultiPolygon() {
      return polylineEncodedMultiPolygon;
    }

    public void setPolylineEncodedMultiPolygon(
      List<List<String>> polylineEncodedMultiPolygon
    ) {
      this.polylineEncodedMultiPolygon = polylineEncodedMultiPolygon;
    }
  }
}
