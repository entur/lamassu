package org.entur.lamassu.cache;

import org.entur.lamassu.model.FeedProvider;
import org.entur.lamassu.model.gbfs.v2_1.GBFSBase;
import org.entur.lamassu.model.gbfs.v2_1.GBFSFeedName;

public interface GBFSFeedCache {
    GBFSBase find(GBFSFeedName feedName, String codespace, String city, String vehicleType);
    void update(GBFSFeedName feedName, FeedProvider feedProvider, GBFSBase feed);
}
