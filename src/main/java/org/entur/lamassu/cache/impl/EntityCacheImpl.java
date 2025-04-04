package org.entur.lamassu.cache.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.entur.lamassu.cache.EntityCache;
import org.entur.lamassu.cache.EntityListener;
import org.entur.lamassu.model.entities.Entity;
import org.redisson.api.RMapCache;
import org.redisson.api.map.event.EntryCreatedListener;
import org.redisson.api.map.event.EntryEvent;
import org.redisson.api.map.event.EntryExpiredListener;
import org.redisson.api.map.event.EntryRemovedListener;
import org.redisson.api.map.event.EntryUpdatedListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class EntityCacheImpl<T extends Entity> implements EntityCache<T> {

  RMapCache<String, T> cache;

  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private final Map<Integer, EntityListener<T>> listeners = new ConcurrentHashMap<>();
  private final AtomicInteger listenerIdCounter = new AtomicInteger(0);
  private final Map<Integer, Integer> redissonListenerIds = new ConcurrentHashMap<>();

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

  @Override
  public void removeAll(Set<String> keys) {
    String[] arr = keys.toArray(String[]::new);
    cache.fastRemoveAsync(arr);
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
  public int addListener(EntityListener<T> listener) {
    int listenerId = listenerIdCounter.incrementAndGet();
    listeners.put(listenerId, listener);
    
    // Register Redisson listeners
    int createdListenerId = cache.addListener(new EntryCreatedListener<String, T>() {
      @Override
      public void onCreated(EntryEvent<String, T> event) {
        listener.onEntityCreated(event.getKey(), event.getValue());
      }
    });
    
    int updatedListenerId = cache.addListener(new EntryUpdatedListener<String, T>() {
      @Override
      public void onUpdated(EntryEvent<String, T> event) {
        listener.onEntityUpdated(event.getKey(), event.getValue());
      }
    });
    
    int removedListenerId = cache.addListener(new EntryRemovedListener<String, T>() {
      @Override
      public void onRemoved(EntryEvent<String, T> event) {
        listener.onEntityDeleted(event.getKey(), event.getValue());
      }
    });
    
    int expiredListenerId = cache.addListener(new EntryExpiredListener<String, T>() {
      @Override
      public void onExpired(EntryEvent<String, T> event) {
        listener.onEntityDeleted(event.getKey(), event.getValue());
      }
    });
    
    // Store the Redisson listener IDs for later removal
    redissonListenerIds.put(listenerId, createdListenerId);
    redissonListenerIds.put(-listenerId, updatedListenerId);  // Using negative values as keys to store multiple IDs
    redissonListenerIds.put(-listenerId * 2, removedListenerId);
    redissonListenerIds.put(-listenerId * 3, expiredListenerId);
    
    return listenerId;
  }
  
  @Override
  public void removeListener(int listenerId) {
    listeners.remove(listenerId);
    
    // Remove all Redisson listeners associated with this ID
    Integer createdListenerId = redissonListenerIds.remove(listenerId);
    Integer updatedListenerId = redissonListenerIds.remove(-listenerId);
    Integer removedListenerId = redissonListenerIds.remove(-listenerId * 2);
    Integer expiredListenerId = redissonListenerIds.remove(-listenerId * 3);
    
    if (createdListenerId != null) {
      cache.removeListener(createdListenerId);
    }
    if (updatedListenerId != null) {
      cache.removeListener(updatedListenerId);
    }
    if (removedListenerId != null) {
      cache.removeListener(removedListenerId);
    }
    if (expiredListenerId != null) {
      cache.removeListener(expiredListenerId);
    }
  }
}
