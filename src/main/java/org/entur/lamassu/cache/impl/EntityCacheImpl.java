package org.entur.lamassu.cache.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.entur.lamassu.cache.EntityCache;
import org.entur.lamassu.cache.EntityListener;
import org.entur.lamassu.model.entities.Entity;
import org.redisson.api.RMapCache;
import org.redisson.api.map.event.EntryCreatedListener;
import org.redisson.api.map.event.EntryRemovedListener;
import org.redisson.api.map.event.EntryUpdatedListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;

abstract class EntityCacheImpl<T extends Entity>
  implements EntityCache<T>, DisposableBean {

  RMapCache<String, T> cache;

  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  // Store Redisson listener IDs for cleanup
  private final List<Integer> redissonListenerIds = new ArrayList<>();

  protected EntityCacheImpl(RMapCache<String, T> cache) {
    this.cache = cache;
  }

  @Override
  public List<T> getAll(Set<String> keys) {
    return new ArrayList<>(getAllAsMap(keys).values());
  }

  @Override
  public List<T> getAll() {
    return new ArrayList<>(cache.values());
  }

  @Override
  public Map<String, T> getAllAsMap(Set<String> keys) {
    try {
      return cache.getAllAsync(keys).get(5, TimeUnit.SECONDS);
    } catch (ExecutionException | TimeoutException e) {
      logger.warn("Unable to fetch entities from cache within 5 seconds", e);
    } catch (InterruptedException e) {
      logger.warn("Interrupted while entities from cache", e);
      Thread.currentThread().interrupt();
    }

    return Map.of();
  }

  @Override
  public T get(String key) {
    try {
      return cache.getAsync(key).get(5, TimeUnit.SECONDS);
    } catch (ExecutionException | TimeoutException e) {
      logger.warn("Unable to fetch entity from cache within 5 seconds", e);
    } catch (InterruptedException e) {
      logger.warn("Interrupted while entity from cache", e);
      Thread.currentThread().interrupt();
    }
    return null;
  }

  @Override
  public void updateAll(Map<String, T> entities) {
    cache.putAll(entities);
  }

  @Override
  public void updateAll(Map<String, T> entities, int ttl, TimeUnit timeUnit) {
    cache.putAll(entities, ttl, timeUnit);
  }

  /**
   * Removes entities from the cache, waiting for the removal to complete.
   *
   * <p>Awaiting matters mostly for visibility: a discarded future means a failed removal is
   * silent, so the entity stays in the cache and keeps being served in the GBFS feeds and by
   * id lookups after it should have been deleted, with no log line, metric or exception to
   * reveal it.
   *
   * <p>It also removes an ordering hazard, though a milder one than the spatial index had:
   * a pending removal could overtake a later write for the same key and delete an entity
   * that was just re-created. The conflicting write here is the next feed tick rather than
   * the adjacent command, so the window was wide but not microscopic.
   */
  @Override
  public void removeAll(Set<String> keys) {
    String[] arr = keys.toArray(String[]::new);
    try {
      cache.fastRemoveAsync(arr).get(5, TimeUnit.SECONDS);
    } catch (ExecutionException | TimeoutException e) {
      logger.warn("Unable to remove entities from cache within 5 seconds", e);
    } catch (InterruptedException e) {
      logger.warn("Interrupted while removing entities from cache", e);
      Thread.currentThread().interrupt();
    }
  }

  @Override
  public Set<String> getKeys() {
    return new HashSet<>(cache.keySet());
  }

  @Override
  public boolean hasKey(String key) {
    try {
      return cache.containsKeyAsync(key).get();
    } catch (InterruptedException e) {
      logger.warn("Interrupted while checking if cache has key", e);
      Thread.currentThread().interrupt();
    } catch (ExecutionException e) {
      logger.warn("Unable to check if cache has key", e);
    }
    return false;
  }

  public int count() {
    return cache.size();
  }

  @Override
  public void addListener(EntityListener<T> listener) {
    synchronized (redissonListenerIds) {
      logger.debug("Added entity listener");

      // Register Redisson listeners
      redissonListenerIds.add(
        cache.addListener(
          (EntryCreatedListener<String, T>) event ->
            listener.onEntityCreated(event.getKey(), event.getValue())
        )
      );

      redissonListenerIds.add(
        cache.addListener(
          (EntryUpdatedListener<String, T>) event ->
            listener.onEntityUpdated(event.getKey(), event.getValue())
        )
      );

      redissonListenerIds.add(
        cache.addListener(
          (EntryRemovedListener<String, T>) event ->
            listener.onEntityDeleted(event.getKey(), event.getValue())
        )
      );
    }
  }

  /**
   * Removes all listeners when the Spring application context is destroyed.
   * This ensures proper cleanup of resources during application shutdown.
   */
  @Override
  public void destroy() {
    logger.info("Removing all entity listeners during application shutdown");

    synchronized (redissonListenerIds) {
      // Remove all Redisson listeners
      int count = redissonListenerIds.size();
      for (Integer redissonId : redissonListenerIds) {
        try {
          cache.removeListener(redissonId);
        } catch (Exception e) {
          logger.warn(
            "Error removing Redisson listener {}: {}",
            redissonId,
            e.getMessage()
          );
        }
      }

      // Clear the collection
      redissonListenerIds.clear();
      logger.info("Removed {} Redisson listeners during shutdown", count);
    }
  }
}
