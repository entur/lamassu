package org.entur.lamassu.cache.impl;

import org.entur.gbfs.v2_2.gbfs.GBFSFeedName;
import org.entur.lamassu.cache.GBFSFeedCacheV2;
import org.entur.lamassu.model.provider.FeedProvider;
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
public class GBFSFeedCacheV2Impl implements GBFSFeedCacheV2 {
    private CacheAsync<String, Object> cache;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public GBFSFeedCacheV2Impl(Cache<String, Object> cache) {
        this.cache = cache.unwrap(CacheAsync.class);
    }

    @Override
    public <T> T find(GBFSFeedName feedName, FeedProvider feedProvider) {
        var key = getKey(feedName, feedProvider.getSystemId());
        try {
            @SuppressWarnings("unchecked") T feed = (T) cache.getAsync(key).get(5, TimeUnit.SECONDS);
            return feed;
        } catch (ExecutionException | TimeoutException e) {
            logger.warn("Unable to fetch feed from cache within 5 second", e);
        } catch (InterruptedException e) {
            logger.warn("Interrupted while fetching feed from cache", e);
            Thread.currentThread().interrupt();
        }
        return null;
    }

    @Override
    public <T> void update(GBFSFeedName feedName, FeedProvider feedProvider, T feed) {
        String key = getKey(
                feedName,
                feedProvider.getSystemId()
        );
        try {
            cache.putAsync(key, feed).get(5, TimeUnit.SECONDS);
        } catch (ExecutionException | TimeoutException e) {
            logger.warn("Unable to update feed cache within 5 second", e);
        } catch (InterruptedException e) {
            logger.warn("Interrupted while updating feed cache", e);
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public <T> T getAndUpdate(GBFSFeedName feedName, FeedProvider feedProvider, T feed) {
        String key = getKey(
                feedName,
                feedProvider.getSystemId()
        );
        try {
            @SuppressWarnings("unchecked") T old = (T) cache.getAndPutAsync(key, feed).get(5, TimeUnit.SECONDS);
            return old;
        } catch (ExecutionException | TimeoutException e) {
            logger.warn("Unable to update feed cache within 5 second", e);
        } catch (InterruptedException e) {
            logger.warn("Interrupted while updating feed cache", e);
            Thread.currentThread().interrupt();
        }
        return null;
    }

    private String getKey(GBFSFeedName feedName, String systemId) {
        return mergeStrings(feedName.value(), systemId);
    }

    private String mergeStrings(String first, String second) {
        return String.format("%s_%s", first, second);
    }
}
