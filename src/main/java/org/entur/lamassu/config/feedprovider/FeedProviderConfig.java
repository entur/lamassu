package org.entur.lamassu.config.feedprovider;

import org.entur.lamassu.model.feedprovider.FeedProvider;

import java.util.List;

public interface FeedProviderConfig {
    List<FeedProvider> getProviders();
}
