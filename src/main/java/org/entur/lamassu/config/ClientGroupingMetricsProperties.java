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

package org.entur.lamassu.config;

import java.util.Set;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "org.entur.lamassu.metrics.client-grouping")
public class ClientGroupingMetricsProperties {

  private boolean enabled = false;
  private String headerName = "ET-Client-Name";
  private Set<String> whitelistedClients = Set.of();
  private String otherClientTag = "other";

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public String getHeaderName() {
    return headerName;
  }

  public void setHeaderName(String headerName) {
    this.headerName = headerName;
  }

  public Set<String> getWhitelistedClients() {
    return whitelistedClients;
  }

  public void setWhitelistedClients(Set<String> whitelistedClients) {
    this.whitelistedClients = whitelistedClients;
  }

  public String getOtherClientTag() {
    return otherClientTag;
  }

  public void setOtherClientTag(String otherClientTag) {
    this.otherClientTag = otherClientTag;
  }

  /**
   * Check if the given client name is in the whitelist (case-insensitive).
   */
  public boolean isWhitelistedClient(String clientName) {
    if (clientName == null || clientName.isBlank()) {
      return false;
    }
    return whitelistedClients.stream().anyMatch(c -> c.equalsIgnoreCase(clientName));
  }

  /**
   * Get the normalized client tag for a given client name.
   * Returns the matching whitelisted name (preserving case from whitelist) or "other".
   */
  public String getClientTag(String clientName) {
    if (clientName == null || clientName.isBlank()) {
      return otherClientTag;
    }
    return whitelistedClients
      .stream()
      .filter(c -> c.equalsIgnoreCase(clientName))
      .findFirst()
      .orElse(otherClientTag);
  }
}
