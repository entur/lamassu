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
      "${org.entur.lamassu.feedprovider.migrate-from-file-to-redis.strategy}"
    ) FeedProviderMergeStrategy mergeStrategy
  ) {
    this.feedProviderConfigRedis = feedProviderConfigRedis;
    this.feedProviderConfigFile = feedProviderConfigFile;
    this.mergeStrategy = mergeStrategy;
  }

  @PostConstruct
  public void init() {
    migrateFeedProviders();
  }

  public void migrateFeedProviders() {
    List<FeedProvider> providers = feedProviderConfigFile.getProviders();
    if (providers == null) {
      providers = new ArrayList<>();
    }

    int migratedCount = 0;

    if (FeedProviderMergeStrategy.REPLACE_ALL.equals(mergeStrategy)) {
      if (feedProviderConfigRedis.saveProviders(providers)) {
        migratedCount = providers.size();
      }
    } else if (FeedProviderMergeStrategy.SKIP.equals(mergeStrategy)) {
      for (FeedProvider provider : providers) {
        if (
          feedProviderConfigRedis.getProviderBySystemId(provider.getSystemId()) == null
        ) {
          if (feedProviderConfigRedis.addProvider(provider)) {
            migratedCount++;
          }
        }
      }
    } else {
      for (FeedProvider provider : providers) {
        if (
          feedProviderConfigRedis.getProviderBySystemId(provider.getSystemId()) == null
        ) {
          if (feedProviderConfigRedis.addProvider(provider)) {
            migratedCount++;
          }
        } else {
          if (feedProviderConfigRedis.updateProvider(provider)) {
            migratedCount++;
          }
        }
      }
    }

    logger.info("Migrated {} feed providers from file to Redis", migratedCount);
  }
}
