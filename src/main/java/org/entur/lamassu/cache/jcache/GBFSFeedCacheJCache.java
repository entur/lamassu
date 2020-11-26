package org.entur.lamassu.cache.jcache;

import org.entur.lamassu.cache.GBFSFeedCache;
import org.entur.lamassu.model.gbfs.v2_1.GBFSBase;
import org.entur.lamassu.model.gbfs.v2_1.GBFSFeedName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.cache.Cache;

@Component
public class GBFSFeedCacheJCache implements GBFSFeedCache {

    @Autowired
    private Cache<String, GBFSBase> cache;

    @Override
    public GBFSBase find(GBFSFeedName feedName, String codespace, String city) {
        String key = feedName.toValue()+ "_" + codespace.toLowerCase() + "_" + city.toLowerCase();
        return cache.get(key);
    }

    @Override
    public void update(GBFSFeedName feedName, String codespace, String city, GBFSBase feed) {
        String key = feedName.toValue()+ "_" + codespace.toLowerCase() + "_" + city.toLowerCase();
        cache.put(key, feed);
    }
}
