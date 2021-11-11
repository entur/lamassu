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

import org.entur.gbfs.authentication.BearerTokenRequestAuthenticator;
import org.entur.gbfs.authentication.Oauth2ClientCredentialsGrantRequestAuthenticator;
import org.entur.gbfs.authentication.RequestAuthenticator;

import java.net.URI;
import java.util.Map;

public class Authentication {
    private AuthenticationScheme scheme;
    private Map<String, String> properties;

    public RequestAuthenticator getRequestAuthenticator() {
        if (scheme == AuthenticationScheme.Oauth2ClientCredentialsGrant) {
            return new Oauth2ClientCredentialsGrantRequestAuthenticator(
                    URI.create(properties.get("tokenUrl")),
                    properties.get("clientId"),
                    properties.get("clientPassword")
            );
        } else if (scheme == AuthenticationScheme.BearerToken) {
            return new BearerTokenRequestAuthenticator(
                    properties.get("accessToken")
            );
        } else if (scheme == AuthenticationScheme.Bolt) {
            return new BoltRequestAuthenticator(
                    properties.get("url"),
                    properties.get("userName"),
                    properties.get("userPass")
            );
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
