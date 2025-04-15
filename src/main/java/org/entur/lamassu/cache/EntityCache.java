package org.entur.lamassu.cache;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.entur.lamassu.model.entities.Entity;

/**
 * Interface for read-write operations on entity caches.
 * Extends EntityReader to inherit all read operations while adding write capabilities.
 */
public interface EntityCache<T extends Entity> extends EntityReader<T> {
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
   * Registers a listener for entity events (create, update, delete).
   *
   * @param listener The listener to register
   */
  void addListener(EntityListener<T> listener);
}
