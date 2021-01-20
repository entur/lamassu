package org.entur.lamassu.listener;

import org.entur.lamassu.model.gbfs.v2_1.GBFSBase;

public interface FeedCacheListener<T extends GBFSBase> {
    void startListening();
    void stopListening();
}
