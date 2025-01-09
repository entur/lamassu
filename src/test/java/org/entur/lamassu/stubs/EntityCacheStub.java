package org.entur.lamassu.stubs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.entur.lamassu.cache.EntityCache;
import org.entur.lamassu.model.entities.Entity;

public class EntityCacheStub<E extends Entity> implements EntityCache<E> {

  private final Map<String, E> map = new HashMap<>();

  @Override
  public List<E> getAll(Set<String> keys) {
    return keys.stream().map(map::get).toList();
  }

  @Override
  public List<E> getAll() {
    return map.values().stream().toList();
  }

  @Override
  public Map<String, E> getAllAsMap(Set<String> keys) {
    Map<String, E> localMap = new HashMap<>();
    keys.stream().forEach(key -> localMap.put(key, map.get(key)));
    return localMap;
  }

  @Override
  public E get(String key) {
    return map.get(key);
  }

  @Override
  public void updateAll(Map<String, E> entities, int ttl, TimeUnit timeUnit) {
    map.putAll(entities);
  }

  @Override
  public void removeAll(Set<String> keys) {
    keys.forEach(map::remove);
  }

  @Override
  public boolean hasKey(String key) {
    return map.containsKey(key);
  }

  @Override
  public int count() {
    return map.size();
  }
}
