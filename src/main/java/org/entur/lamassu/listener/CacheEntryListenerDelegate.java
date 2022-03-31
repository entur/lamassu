package org.entur.lamassu.listener;

import org.redisson.api.ExpiredObjectListener;

import javax.cache.event.CacheEntryEvent;

public interface CacheEntryListenerDelegate<T> extends ExpiredObjectListener {}

