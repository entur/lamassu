package org.entur.lamassu.service.impl;

import org.entur.lamassu.config.feedprovider.FeedProviderConfig;
import org.entur.lamassu.mapper.DiscoveryFeedMapper;
import org.entur.lamassu.model.FeedProviderDiscovery;
import org.entur.lamassu.service.ProviderDiscoveryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.stream.Collectors;

@Component
public class ProviderDiscoveryServiceImpl implements ProviderDiscoveryService {
    FeedProviderDiscovery feedProviderDiscovery;

    private ProviderDiscoveryServiceImpl(
            @Autowired FeedProviderConfig feedProviderConfig,
            @Autowired DiscoveryFeedMapper discoveryFeedMapper
            ) {

        feedProviderDiscovery = new FeedProviderDiscovery();
        feedProviderDiscovery.setFeedProviders(
                feedProviderConfig.getProviders().stream()
                        .map(discoveryFeedMapper::mapFeedProvider).collect(Collectors.toList())
        );
    }

    @Override
    public FeedProviderDiscovery getFeedProviderDiscovery() {
        return feedProviderDiscovery;
    }
}
