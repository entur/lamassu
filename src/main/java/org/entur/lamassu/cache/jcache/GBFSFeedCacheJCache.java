package org.entur.lamassu.cache.jcache;

import org.entur.lamassu.cache.GBFSFeedCache;
import org.entur.lamassu.model.FeedProvider;
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
    public GBFSBase find(GBFSFeedName feedName, String codespace, String city, String vehicleType) {
        return cache.get(getKey(feedName, codespace, city, vehicleType));
    }

    @Override
    public void update(GBFSFeedName feedName, FeedProvider feedProvider, GBFSBase feed) {
        String key = getKey(
                feedName,
                feedProvider.getCodespace(),
                feedProvider.getCity(),
                feedProvider.getVehicleType()
        );
        cache.put(key, feed);
    }

    private String getKey(GBFSFeedName feedName, String codespace, String city, String vehicleType) {
        String key = mergeStrings(feedName.toValue(), codespace);
        if (city != null) {
            key = mergeStrings(key, city);
        }
        if (vehicleType != null) {
            key = mergeStrings(key, vehicleType);
        }
        return key.toLowerCase();
    }

    private String mergeStrings(String first, String second) {
        return String.format("%s_%s", first, second);
    }
}
