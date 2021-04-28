package org.entur.lamassu.config.feedprovider;

import org.entur.lamassu.model.discovery.FeedProvider;
import org.entur.lamassu.util.YamlPropertySourceFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.List;
import java.util.NoSuchElementException;

@Configuration
@ConfigurationProperties(prefix = "lamassu")
@PropertySource(value = "${org.entur.lamassu.feedproviders}", factory = YamlPropertySourceFactory.class)
public class FeedProviderConfigFile implements FeedProviderConfig {
    private List<FeedProvider> providers;

    @Override
    public List<FeedProvider> getProviders() {
        return providers;
    }

    public void setProviders(List<FeedProvider> providers) {
        this.providers = providers;
    }
}
