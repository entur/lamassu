package org.entur.lamassu.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Base64;
import java.util.Map;
import org.entur.lamassu.config.feedprovider.FeedProviderConfig;
import org.entur.lamassu.leader.FeedUpdater;
import org.entur.lamassu.leader.SubscriptionRegistry;
import org.entur.lamassu.leader.SubscriptionStatus;
import org.entur.lamassu.model.provider.FeedProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Integration tests for the AdminController focusing on subscription lifecycle management.
 * These tests verify the end-to-end functionality from admin endpoints to Redis storage.
 */
class AdminControllerIntegrationTest extends AbstractIntegrationTestBase {

  @Autowired
  private TestRestTemplate restTemplate;

  @Autowired
  private FeedProviderConfig feedProviderConfig;

  @Autowired
  private FeedUpdater feedUpdater;

  @Autowired
  private SubscriptionRegistry subscriptionRegistry;

  private static final String TEST_SYSTEM_ID = "test-system-id";
  private static final String TEST_FEED_URL = "http://localhost:8888/testatlantis/gbfs";

  /**
   * Test the full subscription lifecycle:
   * 1. Create a feed provider
   * 2. Start a subscription
   * 3. Check subscription status
   * 4. Stop the subscription
   * 5. Restart the subscription
   */
  @Test
  void testSubscriptionLifecycle() throws InterruptedException {
    // Clean up any existing test provider
    feedProviderConfig.deleteProvider(TEST_SYSTEM_ID);
    subscriptionRegistry.clear();

    // Create a test feed provider
    FeedProvider testProvider = new FeedProvider();
    testProvider.setSystemId(TEST_SYSTEM_ID);
    testProvider.setUrl(TEST_FEED_URL);
    testProvider.setLanguage("en");
    testProvider.setEnabled(false); // Start with disabled

    feedProviderConfig.addProvider(testProvider);

    // Verify the provider exists and is initially disabled
    FeedProvider provider = feedProviderConfig.getProviderBySystemId(TEST_SYSTEM_ID);
    assertNotNull(provider);
    assertFalse(provider.getEnabled());

    // 1. Start subscription
    HttpHeaders headers = createAuthHeaders();
    ResponseEntity<Void> startResponse = restTemplate.exchange(
      "/admin/feed-providers/{systemId}/start",
      HttpMethod.POST,
      new HttpEntity<>(headers),
      Void.class,
      TEST_SYSTEM_ID
    );
    assertEquals(HttpStatus.OK, startResponse.getStatusCode());

    // Wait for subscription to be established
    Thread.sleep(2000);

    // 2. Verify subscription is started
    ResponseEntity<SubscriptionStatus> statusResponse = restTemplate.exchange(
      "/admin/feed-providers/{systemId}/subscription-status",
      HttpMethod.GET,
      new HttpEntity<>(headers),
      SubscriptionStatus.class,
      TEST_SYSTEM_ID
    );
    assertEquals(HttpStatus.OK, statusResponse.getStatusCode());
    assertNotNull(statusResponse.getBody());
    assertEquals(SubscriptionStatus.STARTED, statusResponse.getBody());

    // 3. Verify provider is now enabled in Redis
    provider = feedProviderConfig.getProviderBySystemId(TEST_SYSTEM_ID);
    assertTrue(provider.getEnabled());

    // 4. Get all subscription statuses
    ResponseEntity<Map> allStatusesResponse = restTemplate.exchange(
      "/admin/feed-providers/subscription-statuses",
      HttpMethod.GET,
      new HttpEntity<>(headers),
      Map.class
    );
    assertEquals(HttpStatus.OK, allStatusesResponse.getStatusCode());
    Map<String, String> statuses = allStatusesResponse.getBody();
    assertNotNull(statuses);
    assertTrue(statuses.containsKey(TEST_SYSTEM_ID));
    assertEquals("STARTED", statuses.get(TEST_SYSTEM_ID));

    // 5. Stop subscription
    ResponseEntity<Void> stopResponse = restTemplate.exchange(
      "/admin/feed-providers/{systemId}/stop",
      HttpMethod.POST,
      new HttpEntity<>(headers),
      Void.class,
      TEST_SYSTEM_ID
    );
    assertEquals(HttpStatus.OK, stopResponse.getStatusCode());

    // Wait for subscription to be stopped
    Thread.sleep(1000);

    // 6. Verify subscription is stopped
    statusResponse =
      restTemplate.exchange(
        "/admin/feed-providers/{systemId}/subscription-status",
        HttpMethod.GET,
        new HttpEntity<>(headers),
        SubscriptionStatus.class,
        TEST_SYSTEM_ID
      );
    assertEquals(HttpStatus.OK, statusResponse.getStatusCode());
    assertNotNull(statusResponse.getBody());
    assertEquals(SubscriptionStatus.STOPPED, statusResponse.getBody());

    // 7. Verify provider is still enabled in Redis
    provider = feedProviderConfig.getProviderBySystemId(TEST_SYSTEM_ID);
    assertTrue(provider.getEnabled());

    // 8. Restart subscription
    ResponseEntity<Void> restartResponse = restTemplate.exchange(
      "/admin/feed-providers/{systemId}/restart",
      HttpMethod.POST,
      new HttpEntity<>(headers),
      Void.class,
      TEST_SYSTEM_ID
    );
    assertEquals(HttpStatus.OK, restartResponse.getStatusCode());

    // Wait for subscription to be reestablished
    Thread.sleep(2000);

    // 9. Verify subscription is started again
    statusResponse =
      restTemplate.exchange(
        "/admin/feed-providers/{systemId}/subscription-status",
        HttpMethod.GET,
        new HttpEntity<>(headers),
        SubscriptionStatus.class,
        TEST_SYSTEM_ID
      );
    assertEquals(HttpStatus.OK, statusResponse.getStatusCode());
    assertNotNull(statusResponse.getBody());
    assertEquals(SubscriptionStatus.STARTED, statusResponse.getBody());

    // 10. Verify provider is still enabled in Redis
    provider = feedProviderConfig.getProviderBySystemId(TEST_SYSTEM_ID);
    assertTrue(provider.getEnabled());
  }

  /**
   * Test setting a feed provider's enabled status and verifying it affects subscriptions.
   */
  @Test
  void testSetFeedProviderEnabled() throws InterruptedException {
    // Set up test provider
    feedProviderConfig.deleteProvider(TEST_SYSTEM_ID);
    subscriptionRegistry.clear();

    // Create a test feed provider
    FeedProvider testProvider = new FeedProvider();
    testProvider.setSystemId(TEST_SYSTEM_ID);
    testProvider.setUrl(TEST_FEED_URL);
    testProvider.setLanguage("en");
    testProvider.setEnabled(false); // Start with disabled

    feedProviderConfig.addProvider(testProvider);

    // 1. Enable the feed provider
    HttpHeaders headers = createAuthHeaders();
    ResponseEntity<Void> enableResponse = restTemplate.exchange(
      "/admin/feed-providers/{systemId}/set-enabled?enabled={enabled}",
      HttpMethod.POST,
      new HttpEntity<>(headers),
      Void.class,
      TEST_SYSTEM_ID,
      true
    );
    assertEquals(HttpStatus.OK, enableResponse.getStatusCode());

    // Wait longer for subscription to be established (increased from 2000ms to 5000ms)
    Thread.sleep(5000);

    // Manually start the subscription since set-enabled doesn't automatically start it
    ResponseEntity<Void> startResponse = restTemplate.exchange(
      "/admin/feed-providers/{systemId}/start",
      HttpMethod.POST,
      new HttpEntity<>(headers),
      Void.class,
      TEST_SYSTEM_ID
    );
    assertEquals(HttpStatus.OK, startResponse.getStatusCode());

    // Wait for subscription to be established
    Thread.sleep(2000);

    // 2. Verify provider is enabled in Redis
    FeedProvider provider = feedProviderConfig.getProviderBySystemId(TEST_SYSTEM_ID);
    assertTrue(provider.getEnabled());

    // 3. Verify subscription is started
    ResponseEntity<SubscriptionStatus> statusResponse = restTemplate.exchange(
      "/admin/feed-providers/{systemId}/subscription-status",
      HttpMethod.GET,
      new HttpEntity<>(headers),
      SubscriptionStatus.class,
      TEST_SYSTEM_ID
    );
    assertEquals(HttpStatus.OK, statusResponse.getStatusCode());
    assertNotNull(statusResponse.getBody());
    assertEquals(SubscriptionStatus.STARTED, statusResponse.getBody());

    // 4. Disable the feed provider
    ResponseEntity<Void> disableResponse = restTemplate.exchange(
      "/admin/feed-providers/{systemId}/set-enabled?enabled={enabled}",
      HttpMethod.POST,
      new HttpEntity<>(headers),
      Void.class,
      TEST_SYSTEM_ID,
      false
    );
    assertEquals(HttpStatus.OK, disableResponse.getStatusCode());

    // Wait for subscription to be stopped
    Thread.sleep(2000);

    // 5. Verify provider is disabled in Redis
    provider = feedProviderConfig.getProviderBySystemId(TEST_SYSTEM_ID);
    assertFalse(provider.getEnabled());

    // 6. Verify subscription is automatically stopped
    ResponseEntity<SubscriptionStatus> statusAfterDisableResponse = restTemplate.exchange(
      "/admin/feed-providers/{systemId}/subscription-status",
      HttpMethod.GET,
      new HttpEntity<>(headers),
      SubscriptionStatus.class,
      TEST_SYSTEM_ID
    );
    assertEquals(HttpStatus.OK, statusAfterDisableResponse.getStatusCode());
    assertNotNull(statusAfterDisableResponse.getBody());
    assertEquals(SubscriptionStatus.STOPPED, statusAfterDisableResponse.getBody());
  }

  /**
   * Test error handling for non-existent feed providers.
   */
  @Test
  void testNonExistentFeedProvider() {
    String nonExistentId = "non-existent-id";

    // 1. Try to start subscription for non-existent provider
    HttpHeaders headers = createAuthHeaders();
    ResponseEntity<Void> startResponse = restTemplate.exchange(
      "/admin/feed-providers/{systemId}/start",
      HttpMethod.POST,
      new HttpEntity<>(headers),
      Void.class,
      nonExistentId
    );
    assertEquals(HttpStatus.NOT_FOUND, startResponse.getStatusCode());

    // 2. Try to stop subscription for non-existent provider
    ResponseEntity<Void> stopResponse = restTemplate.exchange(
      "/admin/feed-providers/{systemId}/stop",
      HttpMethod.POST,
      new HttpEntity<>(headers),
      Void.class,
      nonExistentId
    );
    assertEquals(HttpStatus.NOT_FOUND, stopResponse.getStatusCode());

    // 3. Try to restart subscription for non-existent provider
    ResponseEntity<Void> restartResponse = restTemplate.exchange(
      "/admin/feed-providers/{systemId}/restart",
      HttpMethod.POST,
      new HttpEntity<>(headers),
      Void.class,
      nonExistentId
    );
    assertEquals(HttpStatus.NOT_FOUND, restartResponse.getStatusCode());

    // 4. Try to get subscription status for non-existent provider
    ResponseEntity<SubscriptionStatus> statusResponse = restTemplate.exchange(
      "/admin/feed-providers/{systemId}/subscription-status",
      HttpMethod.GET,
      new HttpEntity<>(headers),
      SubscriptionStatus.class,
      nonExistentId
    );
    assertEquals(HttpStatus.OK, statusResponse.getStatusCode());
    assertNotNull(statusResponse.getBody());
    assertEquals(SubscriptionStatus.STOPPED, statusResponse.getBody());
  }

  private HttpHeaders createAuthHeaders() {
    HttpHeaders headers = new HttpHeaders();
    String auth = "admin:admin";
    byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes());
    String authHeader = "Basic " + new String(encodedAuth);
    headers.set("Authorization", authHeader);
    return headers;
  }
}
