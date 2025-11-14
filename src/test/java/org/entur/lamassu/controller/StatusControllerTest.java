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

package org.entur.lamassu.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import org.entur.lamassu.config.feedprovider.FeedProviderConfig;
import org.entur.lamassu.leader.SubscriptionStatus;
import org.entur.lamassu.model.dto.PublicFeedProviderStatus;
import org.entur.lamassu.model.dto.PublicFeedProviderStatusMapper;
import org.entur.lamassu.model.provider.Authentication;
import org.entur.lamassu.model.provider.AuthenticationScheme;
import org.entur.lamassu.model.provider.FeedProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class StatusControllerTest {

  @Mock
  private FeedProviderConfig feedProviderConfig;

  @Mock
  private PublicFeedProviderStatusMapper mapper;

  private StatusController statusController;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    statusController = new StatusController(feedProviderConfig, mapper);
  }

  @Test
  void testGetAllFeedProviders() {
    // Arrange
    FeedProvider provider1 = createTestProvider("system1");
    FeedProvider provider2 = createTestProvider("system2");
    List<FeedProvider> providers = Arrays.asList(provider1, provider2);

    PublicFeedProviderStatus publicStatus1 = createPublicStatus("system1");
    PublicFeedProviderStatus publicStatus2 = createPublicStatus("system2");

    when(feedProviderConfig.getProviders()).thenReturn(providers);
    when(mapper.toPublicStatus(provider1)).thenReturn(publicStatus1);
    when(mapper.toPublicStatus(provider2)).thenReturn(publicStatus2);

    // Act
    ResponseEntity<List<PublicFeedProviderStatus>> response =
      statusController.getAllFeedProviders();

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(2, response.getBody().size());
    assertEquals("system1", response.getBody().get(0).getSystemId());
    assertEquals("system2", response.getBody().get(1).getSystemId());
  }

  @Test
  void testGetFeedProviderBySystemId_Found() {
    // Arrange
    FeedProvider provider = createTestProvider("system1");
    PublicFeedProviderStatus publicStatus = createPublicStatus("system1");

    when(feedProviderConfig.getProviderBySystemId("system1")).thenReturn(provider);
    when(mapper.toPublicStatus(provider)).thenReturn(publicStatus);

    // Act
    ResponseEntity<PublicFeedProviderStatus> response =
      statusController.getFeedProviderBySystemId("system1");

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("system1", response.getBody().getSystemId());
  }

  @Test
  void testGetFeedProviderBySystemId_NotFound() {
    // Arrange
    when(feedProviderConfig.getProviderBySystemId("nonexistent")).thenReturn(null);

    // Act
    ResponseEntity<PublicFeedProviderStatus> response =
      statusController.getFeedProviderBySystemId("nonexistent");

    // Assert
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    assertNull(response.getBody());
  }

  private FeedProvider createTestProvider(String systemId) {
    FeedProvider provider = new FeedProvider();
    provider.setSystemId(systemId);
    provider.setOperatorId("OP001");
    provider.setOperatorName("Test Operator");
    provider.setCodespace("TST");
    provider.setVersion("2.3");
    provider.setEnabled(true);
    provider.setUrl("https://example.com/gbfs.json"); // Sensitive

    // Add authentication (sensitive)
    Authentication auth = new Authentication();
    auth.setScheme(AuthenticationScheme.BEARER_TOKEN);
    provider.setAuthentication(auth);

    return provider;
  }

  private PublicFeedProviderStatus createPublicStatus(String systemId) {
    PublicFeedProviderStatus status = new PublicFeedProviderStatus();
    status.setSystemId(systemId);
    status.setOperatorId("OP001");
    status.setOperatorName("Test Operator");
    status.setCodespace("TST");
    status.setVersion("2.3");
    status.setEnabled(true);
    status.setSubscriptionStatus(SubscriptionStatus.STARTED);
    // Note: No url or authentication in public status
    return status;
  }
}
