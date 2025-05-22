package org.entur.lamassu.config.feedprovider;

import java.util.List;
import org.entur.lamassu.model.provider.FeedProvider;

/**
 * Interface for feed provider configuration management.
 * Implementations of this interface provide CRUD operations for managing feed providers
 * from different storage backends (e.g., file-based, Redis, database).
 * <p>
 * This interface is used by the system to retrieve, add, update, and delete feed provider
 * configurations, which are essential for connecting to external GBFS feeds.
 */
public interface FeedProviderConfig {
  /**
   * Retrieves all feed providers from the configuration source.
   *
   * @return A list of all feed providers, or an empty list if none exist
   */
  List<FeedProvider> getProviders();

  /**
   * Retrieves a specific feed provider by its system ID.
   *
   * @param systemId The unique system identifier of the feed provider
   * @return The feed provider with the specified system ID, or null if not found
   */
  FeedProvider getProviderBySystemId(String systemId);

  /**
   * Adds a new feed provider to the configuration.
   * If a provider with the same system ID already exists, the operation will fail.
   *
   * @param feedProvider The feed provider to add
   * @return true if the provider was successfully added, false otherwise
   */
  boolean addProvider(FeedProvider feedProvider);

  /**
   * Updates an existing feed provider in the configuration.
   * The provider is identified by its system ID.
   *
   * @param feedProvider The feed provider with updated information
   * @return true if the provider was successfully updated, false if the provider doesn't exist or update failed
   */
  boolean updateProvider(FeedProvider feedProvider);

  /**
   * Deletes a feed provider from the configuration by its system ID.
   *
   * @param systemId The system ID of the feed provider to delete
   * @return true if the provider was successfully deleted, false if the provider doesn't exist or deletion failed
   */
  boolean deleteProvider(String systemId);
}
