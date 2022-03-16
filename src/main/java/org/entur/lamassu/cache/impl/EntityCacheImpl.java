package org.entur.lamassu.cache.impl;

import org.entur.lamassu.cache.EntityCache;
import org.entur.lamassu.model.entities.Entity;
import org.redisson.api.CacheAsync;
import org.redisson.api.RLocalCachedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.cache.Cache;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

abstract class EntityCacheImpl<T extends Entity> implements EntityCache<T> {
    RLocalCachedMap<String, T> cache;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    protected EntityCacheImpl(RLocalCachedMap<String, T> cache) {
        this.cache = cache;
    }

    @Override
    public List<T> getAll(Set<String> keys) {
        return new ArrayList<>(getAllAsMap(keys).values());
    }

    @Override
    public List<T> getAll() {
        return new ArrayList<>(cache.cachedValues());
    }

    @Override
    public Map<String, T> getAllAsMap(Set<String> keys) {
        try {
            return cache.getAllAsync(keys).get(5, TimeUnit.SECONDS);
        } catch ( ExecutionException | TimeoutException e) {
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
        cache.putAllAsync(entities);
    }

    @Override
    public void removeAll(Set<String> keys) {
        cache.fastRemoveAsync(String.valueOf(keys));
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
}
