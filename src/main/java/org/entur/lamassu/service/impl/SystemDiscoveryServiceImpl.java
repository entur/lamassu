package org.entur.lamassu.service.impl;

import java.util.Date;
import java.util.List;
import org.entur.gbfs.v3_0.manifest.GBFSData;
import org.entur.gbfs.v3_0.manifest.GBFSDataset;
import org.entur.gbfs.v3_0.manifest.GBFSManifest;
import org.entur.gbfs.v3_0.manifest.GBFSVersion;
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

  @Autowired
  public SystemDiscoveryServiceImpl(
    FeedProviderService feedProviderService,
    SystemDiscoveryMapper systemDiscoveryMapper,
    @Value("${org.entur.lamassu.baseUrl}") String baseUrl
  ) {
    this.systemDiscovery = mapSystemDiscovery(feedProviderService, systemDiscoveryMapper);
    this.gbfsManifest = mapGBFSManifest(systemDiscovery, baseUrl);
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

  public GBFSManifest mapGBFSManifest(
    SystemDiscovery mappedSystemDiscovery,
    String baseUrl
  ) {
    return new GBFSManifest()
      .withVersion(GBFSVersion.Version._3_0.toString())
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
                        .withVersion(GBFSVersion.Version._3_0)
                        .withUrl(baseUrl + "/gbfs/v3beta/" + system.getId() + "/gbfs")
                    )
                  )
              )
              .toList()
          )
      );
  }
}
