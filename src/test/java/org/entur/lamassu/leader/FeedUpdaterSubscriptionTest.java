package org.entur.lamassu.leader;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.concurrent.ForkJoinPool;
import org.entur.gbfs.GbfsSubscriptionManager;
import org.entur.gbfs.validation.model.ValidationResult;
import org.entur.lamassu.config.feedprovider.FeedProviderConfig;
import org.entur.lamassu.leader.entityupdater.EntityCachesUpdater;
import org.entur.lamassu.leader.feedcachesupdater.V2FeedCachesUpdater;
import org.entur.lamassu.leader.feedcachesupdater.V3FeedCachesUpdater;
import org.entur.lamassu.mapper.feedmapper.v2.GbfsV2DeliveryMapper;
import org.entur.lamassu.mapper.feedmapper.v3.GbfsV3DeliveryMapper;
import org.entur.lamassu.metrics.MetricsService;
import org.entur.lamassu.model.provider.FeedProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RBucket;
import org.redisson.api.RListMultimap;

/**
 * Unit tests for the subscription lifecycle methods in the FeedUpdater class.
 * These tests use mocks to isolate the subscription management functionality.
 */
@ExtendWith(MockitoExtension.class)
class FeedUpdaterSubscriptionTest {

  @Mock
  private FeedProviderConfig feedProviderConfig;

  @Mock
  private GbfsSubscriptionManager subscriptionManager;

  @Mock
  private GbfsV2DeliveryMapper gbfsV2DeliveryMapper;

  @Mock
  private GbfsV3DeliveryMapper gbfsV3DeliveryMapper;

  @Mock
  private V2FeedCachesUpdater v2FeedCachesUpdater;

  @Mock
  private V3FeedCachesUpdater v3FeedCachesUpdater;

  @Mock
  private EntityCachesUpdater entityCachesUpdater;

  @Mock
  private RListMultimap<String, ValidationResult> validationResultsCache;

  @Mock
  private RBucket<Boolean> cacheReady;

  @Mock
  private MetricsService metricsService;

  @Mock
  private CacheCleanupService cacheCleanupService;

  @Spy
  private SubscriptionRegistry subscriptionRegistry = new SubscriptionRegistry();

  private FeedUpdater feedUpdater;

  private FeedProvider testProvider;
  private static final String SYSTEM_ID = "test-system-id";
  private static final String SUBSCRIPTION_ID = "test-subscription-id";

  @BeforeEach
  void setUp() {
    // Initialize the FeedUpdater manually with all required dependencies
    feedUpdater =
      new FeedUpdater(
        feedProviderConfig,
        gbfsV2DeliveryMapper,
        gbfsV3DeliveryMapper,
        v2FeedCachesUpdater,
        v3FeedCachesUpdater,
        entityCachesUpdater,
        validationResultsCache,
        cacheReady,
        metricsService,
        cacheCleanupService,
        subscriptionRegistry
      );

    // Set up a test feed provider
    testProvider = new FeedProvider();
    testProvider.setSystemId(SYSTEM_ID);
    testProvider.setUrl("http://test.url/gbfs");
    testProvider.setLanguage("en");

    // Initialize the updaterThreadPool and subscriptionManager fields
    try {
      // Set up the updaterThreadPool
      ForkJoinPool threadPool = new ForkJoinPool(1);
      java.lang.reflect.Field threadPoolField =
        FeedUpdater.class.getDeclaredField("updaterThreadPool");
      threadPoolField.setAccessible(true);
      threadPoolField.set(feedUpdater, threadPool);

      // Set the subscription manager field in FeedUpdater
      java.lang.reflect.Field managerField =
        FeedUpdater.class.getDeclaredField("subscriptionManager");
      managerField.setAccessible(true);
      managerField.set(feedUpdater, subscriptionManager);
    } catch (Exception e) {
      throw new RuntimeException("Failed to set required fields", e);
    }

    // Use lenient() for all common stubbing to avoid UnnecessaryStubbingException
    lenient()
      .when(feedProviderConfig.getProviderBySystemId(SYSTEM_ID))
      .thenReturn(testProvider);
  }

  @Test
  void testStartSubscription() {
    // Act
    boolean result = feedUpdater.startSubscription(testProvider);

    // Assert
    assertTrue(result);
    assertTrue(testProvider.getEnabled());
    verify(subscriptionRegistry)
      .updateSubscriptionStatus(SYSTEM_ID, SubscriptionStatus.STARTING);
  }

  @Test
  void testStartSubscriptionAlreadyExists() {
    // Arrange
    subscriptionRegistry.registerSubscription(SYSTEM_ID, SUBSCRIPTION_ID);

    // Act
    boolean result = feedUpdater.startSubscription(testProvider);

    // Assert
    assertTrue(result);
    // Verify that no new subscription was created
    verify(subscriptionRegistry, never()).updateSubscriptionStatus(any(), any());
  }

  @Test
  void testStopSubscription() {
    // Arrange
    subscriptionRegistry.registerSubscription(SYSTEM_ID, SUBSCRIPTION_ID);
    doNothing().when(subscriptionManager).unsubscribe(SUBSCRIPTION_ID);

    // Act
    boolean result = feedUpdater.stopSubscription(testProvider);

    // Assert
    assertTrue(result);
    assertTrue(testProvider.getEnabled());
    verify(subscriptionRegistry)
      .updateSubscriptionStatus(SYSTEM_ID, SubscriptionStatus.STOPPING);
    verify(subscriptionManager).unsubscribe(SUBSCRIPTION_ID);
    verify(subscriptionRegistry).removeSubscription(SYSTEM_ID);
    verify(cacheCleanupService).clearCacheForSystem(SYSTEM_ID);
    verify(subscriptionRegistry)
      .updateSubscriptionStatus(SYSTEM_ID, SubscriptionStatus.STOPPED);
  }

  @Test
  void testStopSubscriptionNoExistingSubscription() {
    // Act
    boolean result = feedUpdater.stopSubscription(testProvider);

    // Assert
    assertTrue(result);
    assertTrue(testProvider.getEnabled());
    verify(subscriptionRegistry)
      .updateSubscriptionStatus(SYSTEM_ID, SubscriptionStatus.STOPPED);
    verify(subscriptionManager, never()).unsubscribe(anyString());
  }

  @Test
  void testRestartSubscription() {
    // Arrange
    subscriptionRegistry.registerSubscription(SYSTEM_ID, SUBSCRIPTION_ID);
    doNothing().when(subscriptionManager).unsubscribe(SUBSCRIPTION_ID);

    // Act
    boolean result = feedUpdater.restartSubscription(testProvider);

    // Assert
    assertTrue(result);
    assertTrue(testProvider.getEnabled());
    verify(subscriptionRegistry)
      .updateSubscriptionStatus(SYSTEM_ID, SubscriptionStatus.STARTING);
    verify(subscriptionManager).unsubscribe(SUBSCRIPTION_ID);
    verify(subscriptionRegistry).removeSubscription(SYSTEM_ID);
  }

  @Test
  void testRestartSubscriptionNoExistingSubscription() {
    // Act
    boolean result = feedUpdater.restartSubscription(testProvider);

    // Assert
    assertTrue(result);
    assertTrue(testProvider.getEnabled());
    verify(subscriptionRegistry)
      .updateSubscriptionStatus(SYSTEM_ID, SubscriptionStatus.STARTING);
    verify(subscriptionManager, never()).unsubscribe(anyString());
  }

  @Test
  void testGetSubscriptionRegistry() {
    // Act
    SubscriptionRegistry registry = feedUpdater.getSubscriptionRegistry();

    // Assert
    assertSame(registry, subscriptionRegistry);
  }
}
