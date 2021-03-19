package org.entur.lamassu.cache.impl;

import org.entur.lamassu.cache.GBFSFeedCache;
import org.entur.lamassu.model.feedprovider.FeedProvider;
import org.entur.lamassu.model.gbfs.v2_1.GBFSBase;
import org.entur.lamassu.model.gbfs.v2_1.GBFSFeedName;
import org.redisson.api.CacheAsync;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.cache.Cache;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
public class GBFSFeedCacheImpl implements GBFSFeedCache {
    private CacheAsync<String, GBFSBase> cache;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public GBFSFeedCacheImpl(Cache<String, GBFSBase> cache) {
        this.cache = cache.unwrap(CacheAsync.class);
    }

    @Override
    public GBFSBase find(GBFSFeedName feedName, FeedProvider feedProvider) {
        var key = getKey(feedName, feedProvider.getName());
        try {
            return cache.getAsync(key).get(1, TimeUnit.SECONDS);
        } catch (ExecutionException | TimeoutException e) {
            logger.warn("Unable to fetch feed from cache within 1 second", e);
        } catch (InterruptedException e) {
            logger.warn("Interrupted while fetching feed from cache", e);
            Thread.currentThread().interrupt();
        }

        return null;
    }

    @Override
    public void update(GBFSFeedName feedName, FeedProvider feedProvider, GBFSBase feed) {
        String key = getKey(
                feedName,
                feedProvider.getName()
        );
        cache.putAsync(key, feed);
    }

    private String getKey(GBFSFeedName feedName, String providerName) {
        return mergeStrings(feedName.toValue(), providerName);
    }

    private String mergeStrings(String first, String second) {
        return String.format("%s_%s", first, second);
    }
}
