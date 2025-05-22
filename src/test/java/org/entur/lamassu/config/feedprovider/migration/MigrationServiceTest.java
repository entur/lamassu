package org.entur.lamassu.config.feedprovider.migration;

import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.entur.lamassu.config.feedprovider.FeedProviderConfigFile;
import org.entur.lamassu.config.feedprovider.FeedProviderConfigRedis;
import org.entur.lamassu.model.provider.FeedProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MigrationServiceTest {

  @Mock
  private FeedProviderConfigRedis feedProviderConfigRedis;

  @Mock
  private FeedProviderConfigFile feedProviderConfigFile;

  private MigrationService migrationService;

  private List<FeedProvider> createTestProviders() {
    FeedProvider provider1 = new FeedProvider();
    provider1.setSystemId("system1");
    provider1.setOperatorName("Operator 1");
    provider1.setUrl("https://example.com/feed1");

    FeedProvider provider2 = new FeedProvider();
    provider2.setSystemId("system2");
    provider2.setOperatorName("Operator 2");
    provider2.setUrl("https://example.com/feed2");

    return Arrays.asList(provider1, provider2);
  }

  @Test
  void testMigrateFeedProvidersWithReplaceAllStrategy() {
    // Arrange
    List<FeedProvider> providers = createTestProviders();
    when(feedProviderConfigFile.getProviders()).thenReturn(providers);
    when(feedProviderConfigRedis.saveProviders(providers)).thenReturn(true);

    migrationService =
      new MigrationService(
        feedProviderConfigRedis,
        feedProviderConfigFile,
        FeedProviderMergeStrategy.REPLACE_ALL
      );

    // Act
    migrationService.migrateFeedProviders();

    // Assert
    verify(feedProviderConfigFile).getProviders();
    verify(feedProviderConfigRedis).saveProviders(providers);
    verifyNoMoreInteractions(feedProviderConfigRedis);
  }

  @Test
  void testMigrateFeedProvidersWithReplaceAllStrategyFailure() {
    // Arrange
    List<FeedProvider> providers = createTestProviders();
    when(feedProviderConfigFile.getProviders()).thenReturn(providers);
    when(feedProviderConfigRedis.saveProviders(providers)).thenReturn(false);

    migrationService =
      new MigrationService(
        feedProviderConfigRedis,
        feedProviderConfigFile,
        FeedProviderMergeStrategy.REPLACE_ALL
      );

    // Act
    migrationService.migrateFeedProviders();

    // Assert
    verify(feedProviderConfigFile).getProviders();
    verify(feedProviderConfigRedis).saveProviders(providers);
    verifyNoMoreInteractions(feedProviderConfigRedis);
  }

  @Test
  void testMigrateFeedProvidersWithSkipStrategy() {
    // Arrange
    List<FeedProvider> providers = createTestProviders();
    when(feedProviderConfigFile.getProviders()).thenReturn(providers);

    // First provider doesn't exist in Redis
    when(feedProviderConfigRedis.getProviderBySystemId("system1")).thenReturn(null);
    when(feedProviderConfigRedis.addProvider(providers.get(0))).thenReturn(true);

    // Second provider already exists in Redis
    when(feedProviderConfigRedis.getProviderBySystemId("system2"))
      .thenReturn(providers.get(1));

    migrationService =
      new MigrationService(
        feedProviderConfigRedis,
        feedProviderConfigFile,
        FeedProviderMergeStrategy.SKIP
      );

    // Act
    migrationService.migrateFeedProviders();

    // Assert
    verify(feedProviderConfigFile).getProviders();
    verify(feedProviderConfigRedis).getProviderBySystemId("system1");
    verify(feedProviderConfigRedis).addProvider(providers.get(0));
    verify(feedProviderConfigRedis).getProviderBySystemId("system2");
    verifyNoMoreInteractions(feedProviderConfigRedis);
  }

  @Test
  void testMigrateFeedProvidersWithReplaceStrategy() {
    // Arrange
    List<FeedProvider> providers = createTestProviders();
    when(feedProviderConfigFile.getProviders()).thenReturn(providers);

    // First provider doesn't exist in Redis
    when(feedProviderConfigRedis.getProviderBySystemId("system1")).thenReturn(null);
    when(feedProviderConfigRedis.addProvider(providers.get(0))).thenReturn(true);

    // Second provider already exists in Redis
    when(feedProviderConfigRedis.getProviderBySystemId("system2"))
      .thenReturn(providers.get(1));
    when(feedProviderConfigRedis.updateProvider(providers.get(1))).thenReturn(true);

    migrationService =
      new MigrationService(
        feedProviderConfigRedis,
        feedProviderConfigFile,
        FeedProviderMergeStrategy.REPLACE
      );

    // Act
    migrationService.migrateFeedProviders();

    // Assert
    verify(feedProviderConfigFile).getProviders();
    verify(feedProviderConfigRedis).getProviderBySystemId("system1");
    verify(feedProviderConfigRedis).addProvider(providers.get(0));
    verify(feedProviderConfigRedis).getProviderBySystemId("system2");
    verify(feedProviderConfigRedis).updateProvider(providers.get(1));
    verifyNoMoreInteractions(feedProviderConfigRedis);
  }

  @Test
  void testMigrateFeedProvidersWithNullProviders() {
    // Arrange
    when(feedProviderConfigFile.getProviders()).thenReturn(null);

    migrationService =
      new MigrationService(
        feedProviderConfigRedis,
        feedProviderConfigFile,
        FeedProviderMergeStrategy.REPLACE_ALL
      );

    // Act
    migrationService.migrateFeedProviders();

    // Assert
    verify(feedProviderConfigFile).getProviders();
    verify(feedProviderConfigRedis).saveProviders(new ArrayList<>());
    verifyNoMoreInteractions(feedProviderConfigRedis);
  }

  @Test
  void testMigrateFeedProvidersWithFailedAdd() {
    // Arrange
    List<FeedProvider> providers = createTestProviders();
    when(feedProviderConfigFile.getProviders()).thenReturn(providers);

    // Provider doesn't exist in Redis but add fails
    when(feedProviderConfigRedis.getProviderBySystemId("system1")).thenReturn(null);
    when(feedProviderConfigRedis.addProvider(providers.get(0))).thenReturn(false);

    when(feedProviderConfigRedis.getProviderBySystemId("system2")).thenReturn(null);
    when(feedProviderConfigRedis.addProvider(providers.get(1))).thenReturn(true);

    migrationService =
      new MigrationService(
        feedProviderConfigRedis,
        feedProviderConfigFile,
        FeedProviderMergeStrategy.SKIP
      );

    // Act
    migrationService.migrateFeedProviders();

    // Assert
    verify(feedProviderConfigFile).getProviders();
    verify(feedProviderConfigRedis).getProviderBySystemId("system1");
    verify(feedProviderConfigRedis).addProvider(providers.get(0));
    verify(feedProviderConfigRedis).getProviderBySystemId("system2");
    verify(feedProviderConfigRedis).addProvider(providers.get(1));
    verifyNoMoreInteractions(feedProviderConfigRedis);
  }

  @Test
  void testMigrateFeedProvidersWithFailedUpdate() {
    // Arrange
    List<FeedProvider> providers = createTestProviders();
    when(feedProviderConfigFile.getProviders()).thenReturn(providers);

    // First provider exists in Redis but update fails
    when(feedProviderConfigRedis.getProviderBySystemId("system1"))
      .thenReturn(providers.get(0));
    when(feedProviderConfigRedis.updateProvider(providers.get(0))).thenReturn(false);

    // Second provider exists in Redis and update succeeds
    when(feedProviderConfigRedis.getProviderBySystemId("system2"))
      .thenReturn(providers.get(1));
    when(feedProviderConfigRedis.updateProvider(providers.get(1))).thenReturn(true);

    migrationService =
      new MigrationService(
        feedProviderConfigRedis,
        feedProviderConfigFile,
        FeedProviderMergeStrategy.REPLACE
      );

    // Act
    migrationService.migrateFeedProviders();

    // Assert
    verify(feedProviderConfigFile).getProviders();
    verify(feedProviderConfigRedis).getProviderBySystemId("system1");
    verify(feedProviderConfigRedis).updateProvider(providers.get(0));
    verify(feedProviderConfigRedis).getProviderBySystemId("system2");
    verify(feedProviderConfigRedis).updateProvider(providers.get(1));
    verifyNoMoreInteractions(feedProviderConfigRedis);
  }

  @Test
  void testInitCallsMigrateFeedProviders() {
    // Arrange
    migrationService =
      spy(
        new MigrationService(
          feedProviderConfigRedis,
          feedProviderConfigFile,
          FeedProviderMergeStrategy.REPLACE_ALL
        )
      );
    doNothing().when(migrationService).migrateFeedProviders();

    // Act
    migrationService.init();

    // Assert
    verify(migrationService).migrateFeedProviders();
  }
}
