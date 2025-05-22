package org.entur.lamassu.config.feedprovider;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Configuration class for selecting the appropriate FeedProviderConfig implementation
 * based on configuration properties.
 */
@Configuration
public class FeedProviderSourceConfig {

  /**
   * The source to use for feed providers.
   * Possible values: "file" (default) or "redis".
   */
  @Value("${org.entur.lamassu.feedproviders.source:file}")
  private String feedProvidersSource;

  /**
   * Provides the appropriate FeedProviderConfig implementation based on the configured source.
   *
   * @param fileConfig The file-based implementation
   * @param redisConfig The Redis-based implementation
   * @return The selected FeedProviderConfig implementation
   */
  @Bean
  @Primary
  public FeedProviderConfig feedProviderConfig(
    FeedProviderConfigFile fileConfig,
    FeedProviderConfigRedis redisConfig
  ) {
    if ("redis".equalsIgnoreCase(feedProvidersSource)) {
      return redisConfig;
    } else {
      // Default to file-based implementation
      return fileConfig;
    }
  }
}
