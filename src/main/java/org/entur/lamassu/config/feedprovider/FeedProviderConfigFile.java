package org.entur.lamassu.config.feedprovider;

import lombok.Getter;
import lombok.Setter;
import org.entur.lamassu.model.FeedProvider;
import org.entur.lamassu.util.YamlPropertySourceFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "lamassu")
@PropertySource(value = "${org.entur.lamassu.feedproviders}", factory = YamlPropertySourceFactory.class)
public class FeedProviderConfigFile implements FeedProviderConfig {

    @Getter @Setter
    private List<FeedProvider> providers;
}
