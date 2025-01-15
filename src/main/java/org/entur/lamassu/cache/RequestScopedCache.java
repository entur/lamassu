package org.entur.lamassu.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

@Component
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class RequestScopedCache {

  private final Map<String, Object> cache = new ConcurrentHashMap<>();

  public <T> T get(String key, Class<T> type) {
    return type.cast(cache.get(key));
  }

  public void put(String key, Object value) {
    cache.put(key, value);
  }

  public boolean contains(String key) {
    return cache.containsKey(key);
  }

  public void clear() {
    cache.clear();
  }
}
