package org.entur.lamassu.config.feedprovider;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import org.entur.lamassu.model.provider.FeedProvider;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.springframework.stereotype.Component;

/**
 * Redis-backed implementation of FeedProviderConfig.
 * This implementation stores and retrieves feed providers from Redis.
 */
@Component
public class FeedProviderConfigRedis implements FeedProviderConfig {

  private static final String FEED_PROVIDERS_REDIS_KEY = "feedProviders";

  private final RBucket<List<FeedProvider>> feedProvidersBucket;

  public FeedProviderConfigRedis(
    RedissonClient redissonClient,
    ObjectMapper objectMapper
  ) {
    this.feedProvidersBucket =
      redissonClient.getBucket(
        FEED_PROVIDERS_REDIS_KEY,
        new JsonJacksonCodec(objectMapper)
      );
  }

  @Override
  public List<FeedProvider> getProviders() {
    List<FeedProvider> providers = feedProvidersBucket.get();
    return providers != null ? providers : new ArrayList<>();
  }

  /**
   * Adds a new feed provider to the existing list.
   *
   * @param provider The feed provider to add
   * @return true if the operation was successful, false otherwise
   */
  @Override
  public boolean addProvider(FeedProvider provider) {
    List<FeedProvider> providers = getProviders();
    // Check if a provider with the same systemId already exists
    boolean exists = providers
      .stream()
      .anyMatch(p -> p.getSystemId().equals(provider.getSystemId()));

    if (exists) {
      return false; // Provider with this systemId already exists
    }

    providers.add(provider);
    return saveProviders(providers);
  }

  /**
   * Updates an existing feed provider.
   *
   * @param provider The feed provider to update
   * @return true if the operation was successful, false otherwise
   */
  @Override
  public boolean updateProvider(FeedProvider provider) {
    List<FeedProvider> providers = getProviders();
    boolean updated = false;

    for (int i = 0; i < providers.size(); i++) {
      if (providers.get(i).getSystemId().equals(provider.getSystemId())) {
        providers.set(i, provider);
        updated = true;
        break;
      }
    }

    if (!updated) {
      return false; // Provider with this systemId not found
    }

    return saveProviders(providers);
  }

  /**
   * Deletes a feed provider by its systemId.
   *
   * @param systemId The systemId of the feed provider to delete
   * @return true if the operation was successful, false otherwise
   */
  @Override
  public boolean deleteProvider(String systemId) {
    List<FeedProvider> providers = getProviders();
    boolean removed = providers.removeIf(p -> p.getSystemId().equals(systemId));

    if (!removed) {
      return false; // Provider with this systemId not found
    }

    return saveProviders(providers);
  }

  /**
   * Gets a feed provider by its systemId.
   *
   * @param systemId The systemId of the feed provider to get
   * @return The feed provider, or null if not found
   */
  @Override
  public FeedProvider getProviderBySystemId(String systemId) {
    return getProviders()
      .stream()
      .filter(p -> p.getSystemId().equals(systemId))
      .findFirst()
      .orElse(null);
  }

  /**
   * Saves the list of feed providers to Redis.
   *
   * @param providers The list of feed providers to save
   * @return true if the operation was successful, false otherwise
   */
  public boolean saveProviders(List<FeedProvider> providers) {
    feedProvidersBucket.set(providers);
    return true;
  }
}
