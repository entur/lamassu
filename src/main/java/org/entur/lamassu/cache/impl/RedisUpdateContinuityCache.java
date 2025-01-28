package org.entur.lamassu.cache.impl;

import java.util.Date;
import org.entur.lamassu.cache.UpdateContinuityCache;
import org.redisson.api.RMapCache;

/**
 * Redis-backed implementation of UpdateContinuityCache using Redisson.
 */
public class RedisUpdateContinuityCache implements UpdateContinuityCache {

  private final RMapCache<String, Date> cache;

  public RedisUpdateContinuityCache(RMapCache<String, Date> cache) {
    this.cache = cache;
  }

  @Override
  public Date getLastUpdateTime(String systemId) {
    return cache.get(systemId);
  }

  @Override
  public void setLastUpdateTime(String systemId, Date timestamp) {
    if (timestamp == null) {
      cache.remove(systemId);
    } else {
      cache.put(systemId, timestamp);
    }
  }
}
