package org.entur.lamassu.integration;

import org.entur.lamassu.config.FeedProviderConfigFile;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class FeedProviderConfigIntegrationTest extends IntegrationTestBase {

    @Autowired
    FeedProviderConfigFile feedProviderConfig;

    @Test
    void feedProvidersAreInjected() {
        assertThat(feedProviderConfig.getProviders().get(0).getUrl()).isEqualTo("https://test.com/gbfs");
    }
}
