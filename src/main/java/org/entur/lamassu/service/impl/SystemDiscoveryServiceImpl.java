package org.entur.lamassu.service.impl;

import org.entur.lamassu.mapper.entitymapper.SystemDiscoveryMapper;
import org.entur.lamassu.model.discovery.SystemDiscovery;
import org.entur.lamassu.service.FeedAvailabilityService;
import org.entur.lamassu.service.FeedProviderService;
import org.entur.lamassu.service.SystemDiscoveryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class SystemDiscoveryServiceImpl implements SystemDiscoveryService {

    private final FeedProviderService feedProviderService;
    private final FeedAvailabilityService feedAvailabilityService;
    private final SystemDiscoveryMapper systemDiscoveryMapper;

    @Autowired
    public SystemDiscoveryServiceImpl(FeedProviderService feedProviderService, FeedAvailabilityService feedAvailabilityService, SystemDiscoveryMapper systemDiscoveryMapper) {
        this.feedProviderService = feedProviderService;
        this.feedAvailabilityService = feedAvailabilityService;
        this.systemDiscoveryMapper = systemDiscoveryMapper;
    }

    @Override
    public SystemDiscovery getSystemDiscovery() {
        var systemDiscovery = new SystemDiscovery();
        systemDiscovery.setSystems(
                feedAvailabilityService.getAvailableFeeds().keySet().stream()
                        .map(feedProviderService::getFeedProviderBySystemId)
                        .map(systemDiscoveryMapper::mapSystemDiscovery)
                        .collect(Collectors.toList())
        );
        return systemDiscovery;
    }
}
