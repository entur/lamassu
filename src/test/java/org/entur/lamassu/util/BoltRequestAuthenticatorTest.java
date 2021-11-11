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

package org.entur.lamassu.util;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.entur.gbfs.authentication.RequestAuthenticator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;

@WireMockTest
public class BoltRequestAuthenticatorTest {

    @Test
    void testBoltRequestAuthenticator(WireMockRuntimeInfo runtimeInfo) {
        String TEST_TOKEN_URL = "http://localhost:" + runtimeInfo.getHttpPort() + "/token";
        stubFor(post("/token").willReturn(okJson("{\"access_token\":\"fake_token\"}")));

        RequestAuthenticator authenticator = new BoltRequestAuthenticator(
                TEST_TOKEN_URL,
                "user",
                "secret"
        );

        Map<String, String> headers = new HashMap<>();
        authenticator.authenticateRequest(headers);
        Assertions.assertEquals("Bearer fake_token", headers.get("Authorization"));
    }
}
