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

package org.entur.lamassu.model.dto;

import org.entur.lamassu.leader.SubscriptionStatus;

/**
 * Public view of feed provider status without sensitive information.
 * Does not include URL or authentication credentials.
 */
public class PublicFeedProviderStatus {

  private String systemId;
  private String operatorId;
  private String operatorName;
  private String codespace;
  private String version;
  private Boolean enabled;
  private SubscriptionStatus subscriptionStatus;

  // Default constructor
  public PublicFeedProviderStatus() {}

  // Getters and setters
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

  public SubscriptionStatus getSubscriptionStatus() {
    return subscriptionStatus;
  }

  public void setSubscriptionStatus(SubscriptionStatus subscriptionStatus) {
    this.subscriptionStatus = subscriptionStatus;
  }

  @Override
  public String toString() {
    return (
      "PublicFeedProviderStatus{" +
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
      ", version='" +
      version +
      '\'' +
      ", enabled=" +
      enabled +
      ", subscriptionStatus=" +
      subscriptionStatus +
      '}'
    );
  }
}
