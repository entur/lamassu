package org.entur.lamassu.cache.impl;

import org.entur.lamassu.cache.GBFSFeedCache;
import org.entur.lamassu.model.feedprovider.FeedProvider;
import org.entur.lamassu.model.gbfs.v2_1.GBFSBase;
import org.entur.lamassu.model.gbfs.v2_1.GBFSFeedName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.cache.Cache;

@Component
public class GBFSFeedCacheImpl implements GBFSFeedCache {

    @Autowired
    private Cache<String, GBFSBase> cache;

    @Override
    public GBFSBase find(GBFSFeedName feedName, FeedProvider feedProvider) {
        return cache.get(getKey(feedName, feedProvider.getName()));
    }

    @Override
    public void update(GBFSFeedName feedName, FeedProvider feedProvider, GBFSBase feed) {
        String key = getKey(
                feedName,
                feedProvider.getName()
        );
        cache.put(key, feed);
    }

    private String getKey(GBFSFeedName feedName, String providerName) {
        return mergeStrings(feedName.toValue(), providerName);
    }

    private String mergeStrings(String first, String second) {
        return String.format("%s_%s", first, second);
    }


}
