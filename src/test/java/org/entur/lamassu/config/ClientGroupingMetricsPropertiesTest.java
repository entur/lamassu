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

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ClientGroupingMetricsPropertiesTest {

  private ClientGroupingMetricsProperties properties;

  @BeforeEach
  void setUp() {
    properties = new ClientGroupingMetricsProperties();
    properties.setWhitelistedClients(Set.of("app-ios", "app-android", "web-client"));
    properties.setOtherClientTag("other");
  }

  @Test
  void shouldReturnWhitelistedClientExactMatch() {
    assertEquals("app-ios", properties.getClientTag("app-ios"));
  }

  @Test
  void shouldReturnWhitelistedClientCaseInsensitive() {
    assertEquals("app-ios", properties.getClientTag("APP-IOS"));
    assertEquals("app-ios", properties.getClientTag("App-iOS"));
  }

  @Test
  void shouldReturnOtherForUnknownClient() {
    assertEquals("other", properties.getClientTag("unknown-client"));
  }

  @Test
  void shouldReturnOtherForNullClient() {
    assertEquals("other", properties.getClientTag(null));
  }

  @Test
  void shouldReturnOtherForEmptyClient() {
    assertEquals("other", properties.getClientTag(""));
    assertEquals("other", properties.getClientTag("   "));
  }

  @Test
  void shouldCheckWhitelistCorrectly() {
    assertTrue(properties.isWhitelistedClient("app-ios"));
    assertTrue(properties.isWhitelistedClient("APP-IOS"));
    assertFalse(properties.isWhitelistedClient("unknown"));
    assertFalse(properties.isWhitelistedClient(null));
  }

  @Test
  void shouldHaveCorrectDefaultValues() {
    ClientGroupingMetricsProperties defaultProperties =
      new ClientGroupingMetricsProperties();
    assertFalse(defaultProperties.isEnabled());
    assertEquals("ET-Client-Name", defaultProperties.getHeaderName());
    assertEquals("other", defaultProperties.getOtherClientTag());
    assertTrue(defaultProperties.getWhitelistedClients().isEmpty());
  }

  @Test
  void shouldSetAndGetEnabled() {
    properties.setEnabled(true);
    assertTrue(properties.isEnabled());
    properties.setEnabled(false);
    assertFalse(properties.isEnabled());
  }

  @Test
  void shouldSetAndGetHeaderName() {
    properties.setHeaderName("X-Custom-Header");
    assertEquals("X-Custom-Header", properties.getHeaderName());
  }

  @Test
  void shouldSetAndGetOtherClientTag() {
    properties.setOtherClientTag("unknown");
    assertEquals("unknown", properties.getOtherClientTag());
  }
}
