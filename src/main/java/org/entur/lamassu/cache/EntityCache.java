package org.entur.lamassu.cache;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.entur.lamassu.model.entities.Entity;

/**
 * Interface for caching entities
 * Provides methods for retrieving, updating, and removing entities from the cache.
 *
 * @param <T> The type of entity to be cached, must extend Entity
 */
public interface EntityCache<T extends Entity> {
  /**
   * Retrieves multiple entities from the cache by their keys.
   *
   * @param keys Set of entity keys to retrieve
   * @return List of entities found in the cache for the given keys
   */
  List<T> getAll(Set<String> keys);

  /**
   * Retrieves all entities currently stored in the cache.
   *
   * @return List of all entities in the cache
   */
  List<T> getAll();

  /**
   * Retrieves multiple entities from the cache and returns them as a map with their keys.
   *
   * @param keys Set of entity keys to retrieve
   * @return Map of entity keys to their corresponding entities
   */
  Map<String, T> getAllAsMap(Set<String> keys);

  /**
   * Retrieves a single entity from the cache by its key.
   *
   * @param key Key of the entity to retrieve
   * @return The entity if found, or null if not present in the cache
   */
  T get(String key);

  /**
   * Updates or adds multiple entities to the cache with no expiration (infinite TTL).
   * Entities will remain in the cache until explicitly removed.
   *
   * @param entities Map of entity keys to their corresponding entities
   */
  void updateAll(Map<String, T> entities);

  /**
   * Updates or adds multiple entities to the cache with a specified expiration.
   * If ttl is 0 and timeUnit is null, entities will not expire.
   *
   * @param entities Map of entity keys to their corresponding entities
   * @param ttl Cache expiration value, use 0 for no expiration
   * @param timeUnit Cache expiration time unit, use null for no expiration
   */
  void updateAll(Map<String, T> entities, int ttl, TimeUnit timeUnit);

  /**
   * Removes multiple entities from the cache by their keys.
   *
   * @param keys Set of entity keys to remove from the cache
   */
  void removeAll(Set<String> keys);

  /**
   * Checks if an entity exists in the cache.
   *
   * @param key Key of the entity to check
   * @return true if the entity exists in the cache, false otherwise
   */
  boolean hasKey(String key);

  /**
   * Returns the total number of entries currently stored in the cache.
   *
   * @return Number of entries in the cache
   */
  int count();
}
