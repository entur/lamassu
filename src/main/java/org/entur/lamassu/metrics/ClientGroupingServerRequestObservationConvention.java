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

package org.entur.lamassu.metrics;

import io.micrometer.common.KeyValue;
import io.micrometer.common.KeyValues;
import org.entur.lamassu.config.ClientGroupingMetricsProperties;
import org.springframework.http.server.observation.DefaultServerRequestObservationConvention;
import org.springframework.http.server.observation.ServerRequestObservationContext;

/**
 * Custom observation convention that adds a 'client' tag to HTTP server metrics
 * based on the configured header (default: ET-Client-Name).
 *
 * Clients not in the whitelist are grouped under "other" to prevent cardinality explosion.
 */
public class ClientGroupingServerRequestObservationConvention
  extends DefaultServerRequestObservationConvention {

  private static final String CLIENT_TAG_NAME = "client";

  private final ClientGroupingMetricsProperties properties;

  public ClientGroupingServerRequestObservationConvention(
    ClientGroupingMetricsProperties properties
  ) {
    this.properties = properties;
  }

  @Override
  public KeyValues getLowCardinalityKeyValues(ServerRequestObservationContext context) {
    KeyValues keyValues = super.getLowCardinalityKeyValues(context);

    String clientHeaderValue = context.getCarrier().getHeader(properties.getHeaderName());
    String clientTag = properties.getClientTag(clientHeaderValue);

    return keyValues.and(KeyValue.of(CLIENT_TAG_NAME, clientTag));
  }
}
