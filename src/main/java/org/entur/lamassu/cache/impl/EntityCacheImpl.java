package org.entur.lamassu.cache.impl;

import org.entur.lamassu.cache.EntityCache;
import org.entur.lamassu.model.entities.Entity;
import org.redisson.api.CacheAsync;
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
    private final CacheAsync<String, T> cache;
    private final Cache<String, T> syncCache;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    protected EntityCacheImpl(Cache<String, T> cache) {
        this.syncCache = cache;
        this.cache = cache.unwrap(CacheAsync.class);
    }

    @Override
    public List<T> getAll(Set<String> keys) {
        return new ArrayList<>(getAllAsMap(keys).values());
    }

    @Override
    public List<T> getAll() {
        return StreamSupport
                .stream(syncCache.spliterator(), false)
                .map(Cache.Entry::getValue)
                .collect(Collectors.toList());
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
        cache.removeAllAsync(keys);
    }
}
