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
   * Update multiple entities in the cache with a TTL
   */
  void updateAll(Map<String, T> entities, int ttl, TimeUnit timeUnit);

  /**
   * Remove multiple entities from the cache
   */
  void removeAll(Set<String> keys);
}
