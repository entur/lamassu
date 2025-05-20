package org.entur.lamassu.config.feedprovider;

import java.util.List;
import org.entur.lamassu.model.provider.FeedProvider;
import org.entur.lamassu.util.YamlPropertySourceFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ConfigurationProperties(prefix = "lamassu")
@PropertySource(
  value = "${org.entur.lamassu.feedproviders}",
  factory = YamlPropertySourceFactory.class,
  ignoreResourceNotFound = true
)
public class FeedProviderConfigFile implements FeedProviderConfig {

  private List<FeedProvider> providers;

  @Override
  public List<FeedProvider> getProviders() {
    return providers;
  }

  @Override
  public FeedProvider getProviderBySystemId(String systemId) {
    return providers
      .stream()
      .filter(p -> p.getSystemId().equals(systemId))
      .findFirst()
      .orElse(null);
  }

  @Override
  public boolean addProvider(FeedProvider feedProvider) {
    if (
      providers.stream().anyMatch(p -> p.getSystemId().equals(feedProvider.getSystemId()))
    ) {
      return false;
    } else {
      providers.add(feedProvider);
      return true;
    }
  }

  @Override
  public boolean updateProvider(FeedProvider feedProvider) {
    if (
      providers
        .stream()
        .noneMatch(p -> p.getSystemId().equals(feedProvider.getSystemId()))
    ) {
      return false;
    } else {
      providers.removeIf(p -> p.getSystemId().equals(feedProvider.getSystemId()));
      providers.add(feedProvider);
      return true;
    }
  }

  @Override
  public boolean deleteProvider(String systemId) {
    return providers.removeIf(p -> p.getSystemId().equals(systemId));
  }

  public void setProviders(List<FeedProvider> providers) {
    this.providers = providers;
  }
}
