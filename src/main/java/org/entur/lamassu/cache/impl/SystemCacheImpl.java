package org.entur.lamassu.cache.impl;

import org.entur.lamassu.cache.SystemCache;
import org.entur.lamassu.model.entities.System;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.cache.Cache;

@Component
public class SystemCacheImpl extends EntityCacheImpl<System> implements SystemCache {
    protected SystemCacheImpl(@Autowired Cache<String, System> cache) {
        super(cache);
    }
}
