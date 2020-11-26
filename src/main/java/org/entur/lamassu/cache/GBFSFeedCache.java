package org.entur.lamassu.cache;

import org.entur.lamassu.model.gbfs.v2_1.GBFSBase;
import org.entur.lamassu.model.gbfs.v2_1.GBFSFeedName;

public interface GBFSFeedCache {
    GBFSBase find(GBFSFeedName feedName, String codespace, String city);
    void update(GBFSFeedName feedName, String codespace, String city, GBFSBase feed);
}
