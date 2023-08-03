package org.entur.lamassu.config.feedprovider;

import java.util.List;
import org.entur.lamassu.model.provider.FeedProvider;

public interface FeedProviderConfig {
  List<FeedProvider> getProviders();
}
