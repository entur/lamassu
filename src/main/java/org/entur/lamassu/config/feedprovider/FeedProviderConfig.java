package org.entur.lamassu.config.feedprovider;

import java.util.List;
import org.entur.lamassu.model.provider.FeedProvider;

public interface FeedProviderConfig {
  List<FeedProvider> getProviders();

  FeedProvider getProviderBySystemId(String systemId);

  boolean addProvider(FeedProvider feedProvider);

  boolean updateProvider(FeedProvider feedProvider);

  boolean deleteProvider(String systemId);
}
