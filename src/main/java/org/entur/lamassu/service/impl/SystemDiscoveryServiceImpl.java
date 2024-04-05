package org.entur.lamassu.service.impl;

import java.util.Date;
import java.util.List;
import org.entur.gbfs.v3_0_RC2.manifest.GBFSData;
import org.entur.gbfs.v3_0_RC2.manifest.GBFSDataset;
import org.entur.gbfs.v3_0_RC2.manifest.GBFSManifest;
import org.entur.gbfs.v3_0_RC2.manifest.GBFSVersion;
import org.entur.lamassu.mapper.entitymapper.SystemDiscoveryMapper;
import org.entur.lamassu.model.discovery.SystemDiscovery;
import org.entur.lamassu.service.FeedProviderService;
import org.entur.lamassu.service.SystemDiscoveryService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SystemDiscoveryServiceImpl implements SystemDiscoveryService {

  private final SystemDiscovery systemDiscovery;

  private final GBFSManifest gbfsManifest;

  @Value("${org.entur.lamassu.baseUrl}")
  private String baseUrl;

  @Autowired
  public SystemDiscoveryServiceImpl(
    FeedProviderService feedProviderService,
    SystemDiscoveryMapper systemDiscoveryMapper
  ) {
    systemDiscovery = mapSystemDiscovery(feedProviderService, systemDiscoveryMapper);
    gbfsManifest = mapGBFSManifest(systemDiscovery);
  }

  @Override
  public SystemDiscovery getSystemDiscovery() {
    return systemDiscovery;
  }

  @Override
  public GBFSManifest getGBFSManifest() {
    return gbfsManifest;
  }

  @NotNull
  private SystemDiscovery mapSystemDiscovery(
    FeedProviderService feedProviderService,
    SystemDiscoveryMapper systemDiscoveryMapper
  ) {
    var mappedSystemDiscovery = new SystemDiscovery();
    mappedSystemDiscovery.setSystems(
      feedProviderService
        .getFeedProviders()
        .stream()
        .map(systemDiscoveryMapper::mapSystemDiscovery)
        .toList()
    );
    return mappedSystemDiscovery;
  }

  public GBFSManifest mapGBFSManifest(SystemDiscovery mappedSystemDiscovery) {
    return new GBFSManifest()
      .withVersion(GBFSManifest.Version._3_0_RC_2)
      .withLastUpdated(new Date())
      .withTtl(3600)
      .withData(
        new GBFSData()
          .withDatasets(
            mappedSystemDiscovery
              .getSystems()
              .stream()
              .map(system ->
                new GBFSDataset()
                  .withSystemId(system.getId())
                  .withVersions(
                    List.of(
                      new GBFSVersion()
                        .withVersion(GBFSVersion.Version._2_3)
                        .withUrl(system.getUrl()),
                      new GBFSVersion()
                        .withVersion(GBFSVersion.Version._3_0_RC_2)
                        .withUrl(baseUrl + "/gbfs/v3beta/" + system.getId() + "/gbfs")
                    )
                  )
              )
              .toList()
          )
      );
  }
}
