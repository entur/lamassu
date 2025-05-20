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

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.net.URI;
import java.util.Map;
import org.entur.gbfs.authentication.BearerTokenRequestAuthenticator;
import org.entur.gbfs.authentication.HttpHeadersRequestAuthenticator;
import org.entur.gbfs.authentication.Oauth2ClientCredentialsGrantRequestAuthenticator;
import org.entur.gbfs.authentication.RequestAuthenticator;
import org.entur.lamassu.model.provider.AuthenticationScheme;

public class Authentication {

  private AuthenticationScheme scheme;
  private Map<String, String> properties;

  /**
   * Returns the request authenticator based on the authentication scheme.
   * This method is marked with @JsonIgnore to prevent Jackson from trying to serialize
   * the RequestAuthenticator implementations, which don't have proper serializers.
   *
   * @return The appropriate RequestAuthenticator implementation
   */
  @JsonIgnore
  public RequestAuthenticator getRequestAuthenticator() {
    if (scheme == AuthenticationScheme.OAUTH2_CLIENT_CREDENTIALS_GRANT) {
      return new Oauth2ClientCredentialsGrantRequestAuthenticator(
        URI.create(properties.get("tokenUrl")),
        properties.get("clientId"),
        properties.get("clientPassword"),
        properties.get("scope") != null && !properties.get("scope").isEmpty()
          ? properties.get("scope")
          : null
      );
    } else if (scheme == AuthenticationScheme.BEARER_TOKEN) {
      return new BearerTokenRequestAuthenticator(properties.get("accessToken"));
    } else if (scheme == AuthenticationScheme.HTTP_HEADERS) {
      return new HttpHeadersRequestAuthenticator(properties);
    } else {
      return null;
    }
  }

  public AuthenticationScheme getScheme() {
    return scheme;
  }

  public void setScheme(AuthenticationScheme scheme) {
    this.scheme = scheme;
  }

  public Map<String, String> getProperties() {
    return properties;
  }

  public void setProperties(Map<String, String> properties) {
    this.properties = properties;
  }
}
