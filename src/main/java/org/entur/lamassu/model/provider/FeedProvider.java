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

package org.entur.lamassu.model.provider;

import java.util.List;
import org.mobilitydata.gbfs.v2_3.gbfs.GBFSFeedName;
import org.mobilitydata.gbfs.v2_3.system_pricing_plans.GBFSPlan;
import org.mobilitydata.gbfs.v2_3.vehicle_types.GBFSVehicleType;

public class FeedProvider {

  private String systemId;
  private String operatorId;
  private String operatorName;
  private String codespace;
  private String url;
  private String language;
  private Authentication authentication;

  private List<GBFSFeedName> excludeFeeds;
  private Boolean aggregate = true;
  private List<GBFSVehicleType> vehicleTypes;
  private List<GBFSPlan> pricingPlans;

  private String version;
  
  private Boolean enabled = true;

  public String getSystemId() {
    return systemId;
  }

  public void setSystemId(String systemId) {
    this.systemId = systemId;
  }

  public String getOperatorId() {
    return operatorId;
  }

  public void setOperatorId(String operatorId) {
    this.operatorId = operatorId;
  }

  public String getOperatorName() {
    return operatorName;
  }

  public void setOperatorName(String operatorName) {
    this.operatorName = operatorName;
  }

  public String getCodespace() {
    return codespace;
  }

  public void setCodespace(String codespace) {
    this.codespace = codespace;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getLanguage() {
    return language;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  public List<GBFSVehicleType> getVehicleTypes() {
    return vehicleTypes;
  }

  public void setVehicleTypes(List<GBFSVehicleType> vehicleTypes) {
    this.vehicleTypes = vehicleTypes;
  }

  public List<GBFSPlan> getPricingPlans() {
    return pricingPlans;
  }

  public void setPricingPlans(List<GBFSPlan> pricingPlans) {
    this.pricingPlans = pricingPlans;
  }

  public Authentication getAuthentication() {
    return authentication;
  }

  public void setAuthentication(Authentication authentication) {
    this.authentication = authentication;
  }

  public List<GBFSFeedName> getExcludeFeeds() {
    return excludeFeeds;
  }

  public void setExcludeFeeds(List<GBFSFeedName> excludeFeeds) {
    this.excludeFeeds = excludeFeeds;
  }

  public Boolean getAggregate() {
    return aggregate;
  }

  public void setAggregate(Boolean aggregate) {
    this.aggregate = aggregate;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }
  
  public Boolean getEnabled() {
    return enabled;
  }

  public void setEnabled(Boolean enabled) {
    this.enabled = enabled;
  }

  @Override
  public String toString() {
    return (
      "FeedProvider{" +
      "systemId='" +
      systemId +
      '\'' +
      ", operatorId='" +
      operatorId +
      '\'' +
      ", operatorName='" +
      operatorName +
      '\'' +
      ", codespace='" +
      codespace +
      '\'' +
      ", url='" +
      url +
      '\'' +
      ", language='" +
      language +
      '\'' +
      ", authentication=" +
      authentication +
      ", excludeFeeds=" +
      excludeFeeds +
      ", aggregate=" +
      aggregate +
      ", vehicleTypes=" +
      vehicleTypes +
      ", pricingPlans=" +
      pricingPlans +
      ", version='" +
      version +
      '\'' +
      ", enabled=" +
      enabled +
      '}'
    );
  }
}
