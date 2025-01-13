package org.entur.lamassu.cache;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.entur.lamassu.model.entities.Entity;

/**
 * Interface for read-only operations on entity caches.
 * This separation allows for safer handling of caches where write operations
 * should be restricted to specific components.
 */
public interface EntityReader<T extends Entity> {
  /**
   * Get all entities for the given keys
   */
  List<T> getAll(Set<String> keys);

  /**
   * Get all entities in the cache
   */
  List<T> getAll();

  /**
   * Get all entities for the given keys as a map
   */
  Map<String, T> getAllAsMap(Set<String> keys);

  /**
   * Get a single entity by key
   */
  T get(String key);

  /**
   * Check if a key exists in the cache
   */
  boolean hasKey(String key);

  /**
   * Get the total number of entities in the cache
   */
  int count();
}
