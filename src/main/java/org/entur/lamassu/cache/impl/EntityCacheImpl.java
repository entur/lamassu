package org.entur.lamassu.cache.impl;

import org.entur.lamassu.cache.EntityCache;
import org.entur.lamassu.model.Entity;

import javax.cache.Cache;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

abstract class EntityCacheImpl<T extends Entity> implements EntityCache<T> {
    protected final Cache<String, T> cache;

    protected EntityCacheImpl(Cache<String, T> cache) {
        this.cache = cache;
    }

    @Override
    public List<T> getAll(Set<String> keys) {
        return new ArrayList<T>(cache.getAll(keys).values());
    }

    @Override
    public T get(String key) {
        return cache.get(key);
    }

    @Override
    public void updateAll(List<T> entities) {
        cache.putAll(
                entities.stream().reduce(
                        new HashMap<>(),
                        ((acc, entity) -> {
                            acc.put(entity.getId(), entity);
                            return acc;
                        }),
                        ((acc1, acc2) -> {
                            acc1.putAll(acc2);
                            return acc1;
                        })
                )
        );
    }
}
