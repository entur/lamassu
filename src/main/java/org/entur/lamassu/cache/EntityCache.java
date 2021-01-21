package org.entur.lamassu.cache;

import org.entur.lamassu.model.Entity;

import java.util.List;
import java.util.Set;

public interface EntityCache<T extends Entity> {
    List<T> getAll(Set<String> keys);
    T get(String key);
    void updateAll(List<T> entities);
}