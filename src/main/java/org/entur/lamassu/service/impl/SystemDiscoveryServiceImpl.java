package org.entur.lamassu.service.impl;

import java.util.stream.Collectors;
import org.entur.lamassu.mapper.entitymapper.SystemDiscoveryMapper;
import org.entur.lamassu.model.discovery.SystemDiscovery;
import org.entur.lamassu.service.FeedProviderService;
import org.entur.lamassu.service.SystemDiscoveryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SystemDiscoveryServiceImpl implements SystemDiscoveryService {

  private final SystemDiscovery systemDiscovery;

  @Autowired
  public SystemDiscoveryServiceImpl(
    FeedProviderService feedProviderService,
    SystemDiscoveryMapper systemDiscoveryMapper
  ) {
    systemDiscovery = new SystemDiscovery();
    systemDiscovery.setSystems(
      feedProviderService
        .getFeedProviders()
        .stream()
        .map(systemDiscoveryMapper::mapSystemDiscovery)
        .collect(Collectors.toList())
    );
  }

  @Override
  public SystemDiscovery getSystemDiscovery() {
    return systemDiscovery;
  }
}
