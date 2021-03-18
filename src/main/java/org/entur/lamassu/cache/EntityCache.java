package org.entur.lamassu.cache;

import org.entur.lamassu.model.entities.Entity;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface EntityCache<T extends Entity> {
    List<T> getAll(Set<String> keys);
    Map<String, T> getAllAsMap(Set<String> keys);
    T get(String key);
    void updateAll(Map<String, T> entities);
}
