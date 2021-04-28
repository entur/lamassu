package org.entur.lamassu.service.impl;

import org.entur.lamassu.config.feedprovider.FeedProviderConfig;
import org.entur.lamassu.mapper.SystemDiscoveryMapper;
import org.entur.lamassu.model.discovery.SystemDiscovery;
import org.entur.lamassu.service.FeedProviderService;
import org.entur.lamassu.service.SystemDiscoveryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class SystemDiscoveryServiceImpl implements SystemDiscoveryService {
    private final SystemDiscovery systemDiscovery;

    @Autowired
    public SystemDiscoveryServiceImpl(FeedProviderService feedProviderService, SystemDiscoveryMapper systemDiscoveryMapper) {
        systemDiscovery = new SystemDiscovery();
        systemDiscovery.setSystems(
                feedProviderService.getFeedProviders().stream()
                        .map(systemDiscoveryMapper::mapSystemDiscovery).collect(Collectors.toList())
        );
    }

    @Override
    public SystemDiscovery getSystemDiscovery() {
        return systemDiscovery;
    }
}
