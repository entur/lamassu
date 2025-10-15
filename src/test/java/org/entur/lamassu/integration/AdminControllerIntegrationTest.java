package org.entur.lamassu.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Base64;
import java.util.List;
import java.util.Map;
import org.entur.lamassu.config.feedprovider.FeedProviderConfig;
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

  /**
   * Tests the cache management endpoints:
   * 1. Clear old cache
   * 2. Get cache keys
   *
   * This test verifies that the cache management endpoints work correctly
   * without making assumptions about specific Redis keys.
   */
  @Test
  void testCacheManagementEndpoints() {
    // Test clearing old cache
    ResponseEntity<List> clearResponse = restTemplate.exchange(
      "/admin/clear_old_cache",
      HttpMethod.POST,
      createAuthEntity(null),
      List.class
    );
    assertEquals(HttpStatus.OK, clearResponse.getStatusCode());
    assertNotNull(clearResponse.getBody(), "Clear cache response should not be null");

    // Test getting cache keys endpoint
    ResponseEntity<String[]> keysResponse = restTemplate.exchange(
      "/admin/cache_keys",
      HttpMethod.GET,
      createAuthEntity(null),
      String[].class
    );
    assertEquals(HttpStatus.OK, keysResponse.getStatusCode());
    assertNotNull(keysResponse.getBody(), "Cache keys response should not be null");
  }

  private HttpHeaders createAuthHeaders() {
    HttpHeaders headers = new HttpHeaders();
    String auth = "admin:admin";
    byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes());
    String authHeader = "Basic " + new String(encodedAuth);
    headers.set("Authorization", authHeader);
    return headers;
  }

  private HttpEntity createAuthEntity(Object body) {
    return new HttpEntity<>(body, createAuthHeaders());
  }

  /**
   * Test bulk start subscriptions for multiple feed providers.
   */
  @Test
  void testBulkStartSubscriptions() throws InterruptedException {
    // Clean up and create test providers
    String systemId1 = "test-bulk-system-1";
    String systemId2 = "test-bulk-system-2";
    String systemId3 = "test-bulk-system-3";

    feedProviderConfig.deleteProvider(systemId1);
    feedProviderConfig.deleteProvider(systemId2);
    feedProviderConfig.deleteProvider(systemId3);
    subscriptionRegistry.clear();

    // Create test feed providers
    createTestProvider(systemId1, false);
    createTestProvider(systemId2, false);
    createTestProvider(systemId3, false);

    // Bulk start subscriptions
    List<String> systemIds = List.of(systemId1, systemId2, systemId3);
    HttpHeaders headers = createAuthHeaders();
    ResponseEntity<Map> bulkStartResponse = restTemplate.exchange(
      "/admin/feed-providers/bulk/start",
      HttpMethod.POST,
      new HttpEntity<>(systemIds, headers),
      Map.class
    );

    assertEquals(HttpStatus.OK, bulkStartResponse.getStatusCode());
    assertNotNull(bulkStartResponse.getBody());

    Map<String, String> results = bulkStartResponse.getBody();
    assertEquals("SUCCESS", results.get(systemId1));
    assertEquals("SUCCESS", results.get(systemId2));
    assertEquals("SUCCESS", results.get(systemId3));

    // Wait for subscriptions to be established
    Thread.sleep(2000);

    // Verify all providers are enabled
    assertTrue(feedProviderConfig.getProviderBySystemId(systemId1).getEnabled());
    assertTrue(feedProviderConfig.getProviderBySystemId(systemId2).getEnabled());
    assertTrue(feedProviderConfig.getProviderBySystemId(systemId3).getEnabled());

    // Verify all subscriptions are started
    assertEquals(
      SubscriptionStatus.STARTED,
      subscriptionRegistry.getSubscriptionStatusBySystemId(systemId1)
    );
    assertEquals(
      SubscriptionStatus.STARTED,
      subscriptionRegistry.getSubscriptionStatusBySystemId(systemId2)
    );
    assertEquals(
      SubscriptionStatus.STARTED,
      subscriptionRegistry.getSubscriptionStatusBySystemId(systemId3)
    );
  }

  /**
   * Test bulk stop subscriptions for multiple feed providers.
   */
  @Test
  void testBulkStopSubscriptions() throws InterruptedException {
    // Clean up and create test providers
    String systemId1 = "test-bulk-stop-1";
    String systemId2 = "test-bulk-stop-2";

    feedProviderConfig.deleteProvider(systemId1);
    feedProviderConfig.deleteProvider(systemId2);
    subscriptionRegistry.clear();

    // Create and start test providers
    createTestProvider(systemId1, true);
    createTestProvider(systemId2, true);

    HttpHeaders headers = createAuthHeaders();

    // Start the subscriptions first
    restTemplate.exchange(
      "/admin/feed-providers/{systemId}/start",
      HttpMethod.POST,
      new HttpEntity<>(headers),
      Void.class,
      systemId1
    );
    restTemplate.exchange(
      "/admin/feed-providers/{systemId}/start",
      HttpMethod.POST,
      new HttpEntity<>(headers),
      Void.class,
      systemId2
    );

    Thread.sleep(2000);

    // Bulk stop subscriptions
    List<String> systemIds = List.of(systemId1, systemId2);
    ResponseEntity<Map> bulkStopResponse = restTemplate.exchange(
      "/admin/feed-providers/bulk/stop",
      HttpMethod.POST,
      new HttpEntity<>(systemIds, headers),
      Map.class
    );

    assertEquals(HttpStatus.OK, bulkStopResponse.getStatusCode());
    assertNotNull(bulkStopResponse.getBody());

    Map<String, String> results = bulkStopResponse.getBody();
    assertEquals("SUCCESS", results.get(systemId1));
    assertEquals("SUCCESS", results.get(systemId2));

    // Wait for subscriptions to be stopped
    Thread.sleep(1000);

    // Verify all subscriptions are stopped
    assertEquals(
      SubscriptionStatus.STOPPED,
      subscriptionRegistry.getSubscriptionStatusBySystemId(systemId1)
    );
    assertEquals(
      SubscriptionStatus.STOPPED,
      subscriptionRegistry.getSubscriptionStatusBySystemId(systemId2)
    );
  }

  /**
   * Test bulk restart subscriptions for multiple feed providers.
   */
  @Test
  void testBulkRestartSubscriptions() throws InterruptedException {
    // Clean up and create test providers
    String systemId1 = "test-bulk-restart-1";
    String systemId2 = "test-bulk-restart-2";

    feedProviderConfig.deleteProvider(systemId1);
    feedProviderConfig.deleteProvider(systemId2);
    subscriptionRegistry.clear();

    // Create and start test providers
    createTestProvider(systemId1, true);
    createTestProvider(systemId2, true);

    HttpHeaders headers = createAuthHeaders();

    // Start the subscriptions first
    restTemplate.exchange(
      "/admin/feed-providers/{systemId}/start",
      HttpMethod.POST,
      new HttpEntity<>(headers),
      Void.class,
      systemId1
    );
    restTemplate.exchange(
      "/admin/feed-providers/{systemId}/start",
      HttpMethod.POST,
      new HttpEntity<>(headers),
      Void.class,
      systemId2
    );

    Thread.sleep(2000);

    // Bulk restart subscriptions
    List<String> systemIds = List.of(systemId1, systemId2);
    ResponseEntity<Map> bulkRestartResponse = restTemplate.exchange(
      "/admin/feed-providers/bulk/restart",
      HttpMethod.POST,
      new HttpEntity<>(systemIds, headers),
      Map.class
    );

    assertEquals(HttpStatus.OK, bulkRestartResponse.getStatusCode());
    assertNotNull(bulkRestartResponse.getBody());

    Map<String, String> results = bulkRestartResponse.getBody();
    assertEquals("SUCCESS", results.get(systemId1));
    assertEquals("SUCCESS", results.get(systemId2));

    // Wait for subscriptions to be restarted
    Thread.sleep(2000);

    // Verify all subscriptions are started again
    assertEquals(
      SubscriptionStatus.STARTED,
      subscriptionRegistry.getSubscriptionStatusBySystemId(systemId1)
    );
    assertEquals(
      SubscriptionStatus.STARTED,
      subscriptionRegistry.getSubscriptionStatusBySystemId(systemId2)
    );
  }

  /**
   * Test bulk enable/disable feed providers.
   */
  @Test
  void testBulkSetEnabled() throws InterruptedException {
    // Clean up and create test providers
    String systemId1 = "test-bulk-enable-1";
    String systemId2 = "test-bulk-enable-2";
    String systemId3 = "test-bulk-enable-3";

    feedProviderConfig.deleteProvider(systemId1);
    feedProviderConfig.deleteProvider(systemId2);
    feedProviderConfig.deleteProvider(systemId3);
    subscriptionRegistry.clear();

    // Create disabled test providers
    createTestProvider(systemId1, false);
    createTestProvider(systemId2, false);
    createTestProvider(systemId3, false);

    HttpHeaders headers = createAuthHeaders();

    // Test bulk enable
    List<String> systemIds = List.of(systemId1, systemId2, systemId3);
    Map<String, Object> enableRequest = Map.of("systemIds", systemIds, "enabled", true);

    ResponseEntity<Map> bulkEnableResponse = restTemplate.exchange(
      "/admin/feed-providers/bulk/set-enabled",
      HttpMethod.POST,
      new HttpEntity<>(enableRequest, headers),
      Map.class
    );

    assertEquals(HttpStatus.OK, bulkEnableResponse.getStatusCode());
    assertNotNull(bulkEnableResponse.getBody());

    Map<String, String> results = bulkEnableResponse.getBody();
    assertEquals("SUCCESS", results.get(systemId1));
    assertEquals("SUCCESS", results.get(systemId2));
    assertEquals("SUCCESS", results.get(systemId3));

    // Verify all providers are enabled
    assertTrue(feedProviderConfig.getProviderBySystemId(systemId1).getEnabled());
    assertTrue(feedProviderConfig.getProviderBySystemId(systemId2).getEnabled());
    assertTrue(feedProviderConfig.getProviderBySystemId(systemId3).getEnabled());

    // Start subscriptions for testing disable
    restTemplate.exchange(
      "/admin/feed-providers/{systemId}/start",
      HttpMethod.POST,
      new HttpEntity<>(headers),
      Void.class,
      systemId1
    );
    restTemplate.exchange(
      "/admin/feed-providers/{systemId}/start",
      HttpMethod.POST,
      new HttpEntity<>(headers),
      Void.class,
      systemId2
    );

    Thread.sleep(2000);

    // Test bulk disable
    Map<String, Object> disableRequest = Map.of("systemIds", systemIds, "enabled", false);

    ResponseEntity<Map> bulkDisableResponse = restTemplate.exchange(
      "/admin/feed-providers/bulk/set-enabled",
      HttpMethod.POST,
      new HttpEntity<>(disableRequest, headers),
      Map.class
    );

    assertEquals(HttpStatus.OK, bulkDisableResponse.getStatusCode());
    assertNotNull(bulkDisableResponse.getBody());

    results = bulkDisableResponse.getBody();
    assertEquals("SUCCESS", results.get(systemId1));
    assertEquals("SUCCESS", results.get(systemId2));
    assertEquals("SUCCESS", results.get(systemId3));

    // Verify all providers are disabled
    assertFalse(feedProviderConfig.getProviderBySystemId(systemId1).getEnabled());
    assertFalse(feedProviderConfig.getProviderBySystemId(systemId2).getEnabled());
    assertFalse(feedProviderConfig.getProviderBySystemId(systemId3).getEnabled());

    Thread.sleep(1000);

    // Verify subscriptions were stopped when disabled
    assertEquals(
      SubscriptionStatus.STOPPED,
      subscriptionRegistry.getSubscriptionStatusBySystemId(systemId1)
    );
    assertEquals(
      SubscriptionStatus.STOPPED,
      subscriptionRegistry.getSubscriptionStatusBySystemId(systemId2)
    );
  }

  /**
   * Test bulk operations with mixed results (some providers exist, some don't).
   */
  @Test
  void testBulkOperationsWithMixedResults() {
    // Clean up and create test provider
    String existingSystemId = "test-bulk-mixed-existing";
    String nonExistentSystemId = "test-bulk-mixed-nonexistent";

    feedProviderConfig.deleteProvider(existingSystemId);
    subscriptionRegistry.clear();

    // Create only one test provider
    createTestProvider(existingSystemId, false);

    HttpHeaders headers = createAuthHeaders();

    // Test bulk start with mixed results
    List<String> systemIds = List.of(existingSystemId, nonExistentSystemId);
    ResponseEntity<Map> bulkStartResponse = restTemplate.exchange(
      "/admin/feed-providers/bulk/start",
      HttpMethod.POST,
      new HttpEntity<>(systemIds, headers),
      Map.class
    );

    assertEquals(HttpStatus.OK, bulkStartResponse.getStatusCode());
    assertNotNull(bulkStartResponse.getBody());

    Map<String, String> results = bulkStartResponse.getBody();
    assertEquals("SUCCESS", results.get(existingSystemId));
    assertEquals("NOT_FOUND", results.get(nonExistentSystemId));

    // Test bulk enable with mixed results
    Map<String, Object> enableRequest = Map.of("systemIds", systemIds, "enabled", true);

    ResponseEntity<Map> bulkEnableResponse = restTemplate.exchange(
      "/admin/feed-providers/bulk/set-enabled",
      HttpMethod.POST,
      new HttpEntity<>(enableRequest, headers),
      Map.class
    );

    assertEquals(HttpStatus.OK, bulkEnableResponse.getStatusCode());
    assertNotNull(bulkEnableResponse.getBody());

    results = bulkEnableResponse.getBody();
    assertEquals("SUCCESS", results.get(existingSystemId));
    assertEquals("NOT_FOUND", results.get(nonExistentSystemId));
  }

  /**
   * Helper method to create a test feed provider.
   */
  private void createTestProvider(String systemId, boolean enabled) {
    FeedProvider testProvider = new FeedProvider();
    testProvider.setSystemId(systemId);
    testProvider.setUrl(TEST_FEED_URL);
    testProvider.setLanguage("en");
    testProvider.setEnabled(enabled);
    feedProviderConfig.addProvider(testProvider);
  }
}
