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

package org.entur.lamassu.config.feedprovider.migration;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import org.entur.lamassu.config.feedprovider.FeedProviderConfigFile;
import org.entur.lamassu.config.feedprovider.FeedProviderConfigRedis;
import org.entur.lamassu.model.provider.FeedProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Service responsible for migrating feed providers from file-based configuration to Redis.
 * The migration strategy can be configured via application properties.
 */
@ConditionalOnProperty(
  value = "org.entur.lamassu.feedprovider.migrate-from-file-to-redis",
  havingValue = "true"
)
@Component
public class MigrationService {

  private static final Logger logger = LoggerFactory.getLogger(MigrationService.class);
  private final FeedProviderConfigRedis feedProviderConfigRedis;
  private final FeedProviderConfigFile feedProviderConfigFile;
  private final FeedProviderMergeStrategy mergeStrategy;

  public MigrationService(
    FeedProviderConfigRedis feedProviderConfigRedis,
    FeedProviderConfigFile feedProviderConfigFile,
    @Value(
      "${org.entur.lamassu.feedprovider.migrate-from-file-to-redis.strategy:REPLACE_ALL}"
    ) FeedProviderMergeStrategy mergeStrategy
  ) {
    this.feedProviderConfigRedis = feedProviderConfigRedis;
    this.feedProviderConfigFile = feedProviderConfigFile;
    this.mergeStrategy = mergeStrategy;
  }

  /**
   * Initializes the migration process after bean construction.
   */
  @PostConstruct
  public void init() {
    migrateFeedProviders();
  }

  /**
   * Migrates feed providers from file-based configuration to Redis.
   * The migration strategy determines how existing providers in Redis are handled.
   */
  public void migrateFeedProviders() {
    List<FeedProvider> providers = getProvidersFromFile();
    int migratedCount = migrateProvidersBasedOnStrategy(providers);
    logger.info("Migrated {} feed providers from file to Redis", migratedCount);
  }

  /**
   * Gets the list of providers from the file-based configuration.
   * Returns an empty list if no providers are found.
   *
   * @return List of feed providers
   */
  private List<FeedProvider> getProvidersFromFile() {
    List<FeedProvider> providers = feedProviderConfigFile.getProviders();
    return providers != null ? providers : new ArrayList<>();
  }

  /**
   * Migrates providers based on the configured merge strategy.
   *
   * @param providers List of providers to migrate
   * @return Number of successfully migrated providers
   */
  private int migrateProvidersBasedOnStrategy(List<FeedProvider> providers) {
    switch (mergeStrategy) {
      case REPLACE_ALL:
        return migrateWithReplaceAllStrategy(providers);
      case SKIP:
        return migrateWithSkipStrategy(providers);
      case REPLACE:
      default:
        return migrateWithReplaceStrategy(providers);
    }
  }

  /**
   * Migrates providers using the REPLACE_ALL strategy.
   * This strategy replaces all providers in Redis with the ones from the file.
   *
   * @param providers List of providers to migrate
   * @return Number of successfully migrated providers
   */
  private int migrateWithReplaceAllStrategy(List<FeedProvider> providers) {
    return feedProviderConfigRedis.saveProviders(providers) ? providers.size() : 0;
  }

  /**
   * Migrates providers using the SKIP strategy.
   * This strategy only adds providers that don't already exist in Redis.
   *
   * @param providers List of providers to migrate
   * @return Number of successfully migrated providers
   */
  private int migrateWithSkipStrategy(List<FeedProvider> providers) {
    int migratedCount = 0;

    for (FeedProvider provider : providers) {
      if (isProviderMissing(provider) && addProvider(provider)) {
        migratedCount++;
      }
    }

    return migratedCount;
  }

  /**
   * Migrates providers using the REPLACE strategy.
   * This strategy adds new providers and updates existing ones in Redis.
   *
   * @param providers List of providers to migrate
   * @return Number of successfully migrated providers
   */
  private int migrateWithReplaceStrategy(List<FeedProvider> providers) {
    int migratedCount = 0;

    for (FeedProvider provider : providers) {
      if (isProviderMissing(provider)) {
        if (addProvider(provider)) {
          migratedCount++;
        }
      } else if (updateProvider(provider)) {
        migratedCount++;
      }
    }

    return migratedCount;
  }

  /**
   * Checks if a provider does not exist in Redis.
   *
   * @param provider Provider to check
   * @return true if the provider does not exist in Redis, false otherwise
   */
  private boolean isProviderMissing(FeedProvider provider) {
    return feedProviderConfigRedis.getProviderBySystemId(provider.getSystemId()) == null;
  }

  /**
   * Adds a provider to Redis.
   *
   * @param provider Provider to add
   * @return true if the provider was successfully added, false otherwise
   */
  private boolean addProvider(FeedProvider provider) {
    return feedProviderConfigRedis.addProvider(provider);
  }

  /**
   * Updates a provider in Redis.
   *
   * @param provider Provider to update
   * @return true if the provider was successfully updated, false otherwise
   */
  private boolean updateProvider(FeedProvider provider) {
    return feedProviderConfigRedis.updateProvider(provider);
  }
}
