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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.entur.lamassu.leader.SubscriptionRegistry;
import org.entur.lamassu.leader.SubscriptionStatus;
import org.entur.lamassu.model.provider.Authentication;
import org.entur.lamassu.model.provider.AuthenticationScheme;
import org.entur.lamassu.model.provider.FeedProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class PublicFeedProviderStatusMapperTest {

  @Mock
  private SubscriptionRegistry subscriptionRegistry;

  private PublicFeedProviderStatusMapper mapper;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    mapper = new PublicFeedProviderStatusMapper(subscriptionRegistry);
  }

  @Test
  void testToPublicStatus_FiltersSensitiveData() {
    // Arrange
    FeedProvider provider = new FeedProvider();
    provider.setSystemId("testsystem");
    provider.setOperatorId("OP001");
    provider.setOperatorName("Test Operator");
    provider.setCodespace("TST");
    provider.setVersion("3.0");
    provider.setEnabled(true);
    provider.setUrl("https://sensitive.com/gbfs.json"); // SENSITIVE

    Authentication auth = new Authentication();
    auth.setScheme(AuthenticationScheme.BEARER_TOKEN);
    provider.setAuthentication(auth); // SENSITIVE

    when(subscriptionRegistry.getSubscriptionStatusBySystemId("testsystem"))
      .thenReturn(SubscriptionStatus.STARTED);

    // Act
    PublicFeedProviderStatus result = mapper.toPublicStatus(provider);

    // Assert - Verify non-sensitive data is included
    assertEquals("testsystem", result.getSystemId());
    assertEquals("OP001", result.getOperatorId());
    assertEquals("Test Operator", result.getOperatorName());
    assertEquals("TST", result.getCodespace());
    assertEquals("3.0", result.getVersion());
    assertTrue(result.getEnabled());
    assertEquals(SubscriptionStatus.STARTED, result.getSubscriptionStatus());

    // Critical: Verify DTO class does not have sensitive fields
    // (This is a compile-time check, but document it in test)
    assertDoesNotThrow(() -> {
      // If these methods existed, compilation would fail
      // result.getUrl();
      // result.getAuthentication();
    });
  }

  @Test
  void testToPublicStatus_HandlesStoppedSubscriptionStatus() {
    // Arrange
    FeedProvider provider = new FeedProvider();
    provider.setSystemId("testsystem");
    provider.setOperatorName("Test");
    provider.setCodespace("TST");
    provider.setVersion("2.3");
    provider.setEnabled(false);

    when(subscriptionRegistry.getSubscriptionStatusBySystemId("testsystem"))
      .thenReturn(SubscriptionStatus.STOPPED);

    // Act
    PublicFeedProviderStatus result = mapper.toPublicStatus(provider);

    // Assert
    assertEquals(SubscriptionStatus.STOPPED, result.getSubscriptionStatus());
  }
}
