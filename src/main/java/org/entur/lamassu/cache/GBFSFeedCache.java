package org.entur.lamassu.cache;

import org.entur.lamassu.model.feedprovider.FeedProvider;
import org.entur.lamassu.model.gbfs.v2_1.GBFSBase;
import org.entur.lamassu.model.gbfs.v2_1.GBFSFeedName;

public interface GBFSFeedCache {
    GBFSBase find(GBFSFeedName feedName, FeedProvider feedProvider);
    void update(GBFSFeedName feedName, FeedProvider feedProvider, GBFSBase feed);
}
