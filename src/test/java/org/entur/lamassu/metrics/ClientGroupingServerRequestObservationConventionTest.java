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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.micrometer.common.KeyValues;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Set;
import org.entur.lamassu.config.ClientGroupingMetricsProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.server.observation.ServerRequestObservationContext;

class ClientGroupingServerRequestObservationConventionTest {

  private ClientGroupingMetricsProperties properties;
  private ClientGroupingServerRequestObservationConvention convention;

  @BeforeEach
  void setUp() {
    properties = new ClientGroupingMetricsProperties();
    properties.setEnabled(true);
    properties.setHeaderName("ET-Client-Name");
    properties.setWhitelistedClients(Set.of("app-ios", "app-android"));
    properties.setOtherClientTag("other");
    convention = new ClientGroupingServerRequestObservationConvention(properties);
  }

  @Test
  void shouldAddClientTagForWhitelistedClient() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    when(request.getMethod()).thenReturn("GET");
    when(request.getRequestURI()).thenReturn("/api/test");
    when(request.getHeader("ET-Client-Name")).thenReturn("app-ios");

    ServerRequestObservationContext context = new ServerRequestObservationContext(
      request,
      response
    );

    KeyValues keyValues = convention.getLowCardinalityKeyValues(context);

    assertTrue(
      keyValues
        .stream()
        .anyMatch(kv -> kv.getKey().equals("client") && kv.getValue().equals("app-ios"))
    );
  }

  @Test
  void shouldAddOtherTagForUnknownClient() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    when(request.getMethod()).thenReturn("GET");
    when(request.getRequestURI()).thenReturn("/api/test");
    when(request.getHeader("ET-Client-Name")).thenReturn("unknown-client");

    ServerRequestObservationContext context = new ServerRequestObservationContext(
      request,
      response
    );

    KeyValues keyValues = convention.getLowCardinalityKeyValues(context);

    assertTrue(
      keyValues
        .stream()
        .anyMatch(kv -> kv.getKey().equals("client") && kv.getValue().equals("other"))
    );
  }

  @Test
  void shouldAddOtherTagForMissingHeader() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    when(request.getMethod()).thenReturn("GET");
    when(request.getRequestURI()).thenReturn("/api/test");
    when(request.getHeader("ET-Client-Name")).thenReturn(null);

    ServerRequestObservationContext context = new ServerRequestObservationContext(
      request,
      response
    );

    KeyValues keyValues = convention.getLowCardinalityKeyValues(context);

    assertTrue(
      keyValues
        .stream()
        .anyMatch(kv -> kv.getKey().equals("client") && kv.getValue().equals("other"))
    );
  }

  @Test
  void shouldHandleCaseInsensitiveClientMatching() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    when(request.getMethod()).thenReturn("GET");
    when(request.getRequestURI()).thenReturn("/api/test");
    when(request.getHeader("ET-Client-Name")).thenReturn("APP-IOS");

    ServerRequestObservationContext context = new ServerRequestObservationContext(
      request,
      response
    );

    KeyValues keyValues = convention.getLowCardinalityKeyValues(context);

    assertTrue(
      keyValues
        .stream()
        .anyMatch(kv -> kv.getKey().equals("client") && kv.getValue().equals("app-ios"))
    );
  }

  @Test
  void shouldUseCustomHeaderName() {
    properties.setHeaderName("X-Custom-Client");
    convention = new ClientGroupingServerRequestObservationConvention(properties);

    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    when(request.getMethod()).thenReturn("GET");
    when(request.getRequestURI()).thenReturn("/api/test");
    when(request.getHeader("X-Custom-Client")).thenReturn("app-android");

    ServerRequestObservationContext context = new ServerRequestObservationContext(
      request,
      response
    );

    KeyValues keyValues = convention.getLowCardinalityKeyValues(context);

    assertTrue(
      keyValues
        .stream()
        .anyMatch(kv ->
          kv.getKey().equals("client") && kv.getValue().equals("app-android")
        )
    );
  }

  @Test
  void shouldIncludeStandardKeyValuesFromParent() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    when(request.getMethod()).thenReturn("GET");
    when(request.getRequestURI()).thenReturn("/api/test");
    when(request.getHeader("ET-Client-Name")).thenReturn("app-ios");

    ServerRequestObservationContext context = new ServerRequestObservationContext(
      request,
      response
    );

    KeyValues keyValues = convention.getLowCardinalityKeyValues(context);

    // Verify that standard key values from DefaultServerRequestObservationConvention are present
    assertTrue(
      keyValues.stream().anyMatch(kv -> kv.getKey().equals("method")),
      "Should include 'method' key from parent convention"
    );
  }
}
