package org.entur.lamassu.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.entur.lamassu.model.entities.Entity;
import org.springframework.web.context.request.RequestContextHolder;

/**
 * A read-only decorator for EntityReader that adds request-level caching.
 * This implementation only provides read operations and caches results within
 * the scope of an HTTP request.
 */
public class RequestScopedEntityReader<T extends Entity> implements EntityReader<T> {

  private final EntityReader<T> delegate;
  private final RequestScopedCache requestCache;
  private final String cachePrefix;

  public RequestScopedEntityReader(
    EntityReader<T> delegate,
    RequestScopedCache requestCache,
    String cachePrefix
  ) {
    this.delegate = delegate;
    this.requestCache = requestCache;
    this.cachePrefix = cachePrefix;
  }

  private String getCacheKey(String key) {
    return cachePrefix + ":" + key;
  }

  private boolean isRequestContextActive() {
    try {
      return RequestContextHolder.getRequestAttributes() != null;
    } catch (IllegalStateException e) {
      return false;
    }
  }

  private T getFromRequestCache(String key) {
    if (!isRequestContextActive()) {
      return null;
    }
    try {
      String cacheKey = getCacheKey(key);
      return requestCache.get(cacheKey, (Class<T>) Entity.class);
    } catch (Exception e) {
      return null;
    }
  }

  private void putInRequestCache(String key, T value) {
    if (!isRequestContextActive()) {
      return;
    }
    try {
      requestCache.put(getCacheKey(key), value);
    } catch (Exception e) {
      // Ignore cache errors in non-request contexts
    }
  }

  @Override
  public List<T> getAll(Set<String> keys) {
    Map<String, T> result = getAllAsMap(keys);
    return new ArrayList<>(result.values());
  }

  @Override
  public List<T> getAll() {
    return delegate.getAll();
  }

  @Override
  public Map<String, T> getAllAsMap(Set<String> keys) {
    if (!isRequestContextActive()) {
      return delegate.getAllAsMap(keys);
    }

    Map<String, T> result = new HashMap<>();
    Set<String> missingKeys = new HashSet<>();

    // Check request cache first
    for (String key : keys) {
      T value = getFromRequestCache(key);
      if (value != null) {
        result.put(key, value);
      } else {
        missingKeys.add(key);
      }
    }

    // Get missing keys from delegate
    if (!missingKeys.isEmpty()) {
      Map<String, T> delegateResults = delegate.getAllAsMap(missingKeys);
      delegateResults.forEach((key, value) -> {
        result.put(key, value);
        putInRequestCache(key, value);
      });
    }

    return result;
  }

  @Override
  public T get(String key) {
    T value = getFromRequestCache(key);
    if (value == null) {
      value = delegate.get(key);
      if (value != null) {
        putInRequestCache(key, value);
      }
    }
    return value;
  }

  @Override
  public boolean hasKey(String key) {
    if (isRequestContextActive()) {
      T value = getFromRequestCache(key);
      if (value != null) {
        return true;
      }
    }
    return delegate.hasKey(key);
  }

  @Override
  public int count() {
    return delegate.count();
  }
}
