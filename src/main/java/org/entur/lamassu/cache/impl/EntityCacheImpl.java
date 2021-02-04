package org.entur.lamassu.cache.impl;

import org.entur.lamassu.cache.EntityCache;
import org.entur.lamassu.model.entities.Entity;

import javax.cache.Cache;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

abstract class EntityCacheImpl<T extends Entity> implements EntityCache<T> {
    protected final Cache<String, T> cache;

    protected EntityCacheImpl(Cache<String, T> cache) {
        this.cache = cache;
    }

    @Override
    public List<T> getAll(Set<String> keys) {
        return new ArrayList<>(cache.getAll(keys).values());
    }

    @Override
    public T get(String key) {
        return cache.get(key);
    }

    @Override
    public void updateAll(Map<String, T> entities) {
        cache.putAll(entities);
    }
}
