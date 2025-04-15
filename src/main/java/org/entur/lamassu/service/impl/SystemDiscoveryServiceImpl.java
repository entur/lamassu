package org.entur.lamassu.service.impl;

import java.util.Date;
import java.util.List;
import org.entur.lamassu.mapper.entitymapper.SystemDiscoveryMapper;
import org.entur.lamassu.model.discovery.SystemDiscovery;
import org.entur.lamassu.service.FeedProviderService;
import org.entur.lamassu.service.SystemDiscoveryService;
import org.jetbrains.annotations.NotNull;
import org.mobilitydata.gbfs.v3_0.manifest.GBFSData;
import org.mobilitydata.gbfs.v3_0.manifest.GBFSDataset;
import org.mobilitydata.gbfs.v3_0.manifest.GBFSManifest;
import org.mobilitydata.gbfs.v3_0.manifest.GBFSVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SystemDiscoveryServiceImpl implements SystemDiscoveryService {

  private static final Logger logger = LoggerFactory.getLogger(
    SystemDiscoveryServiceImpl.class
  );

  private final FeedProviderService feedProviderService;
  private final SystemDiscoveryMapper systemDiscoveryMapper;
  private final String baseUrl;

  @Autowired
  public SystemDiscoveryServiceImpl(
    FeedProviderService feedProviderService,
    SystemDiscoveryMapper systemDiscoveryMapper,
    @Value("${org.entur.lamassu.baseUrl}") String baseUrl
  ) {
    this.feedProviderService = feedProviderService;
    this.systemDiscoveryMapper = systemDiscoveryMapper;
    this.baseUrl = baseUrl;
  }

  @Override
  public SystemDiscovery getSystemDiscovery() {
    return mapSystemDiscovery();
  }

  @Override
  public GBFSManifest getGBFSManifest() {
    return mapGBFSManifest(mapSystemDiscovery());
  }

  @NotNull
  private SystemDiscovery mapSystemDiscovery() {
    logger.debug(
      "Mapping system discovery with {} feed providers",
      feedProviderService.getFeedProviders().size()
    );

    var mappedSystemDiscovery = new SystemDiscovery();
    var filteredSystems = feedProviderService
      .getFeedProviders()
      .stream()
      .filter(feedProvider -> {
        String systemId = feedProvider.getSystemId();
        Boolean enabled = feedProvider.getEnabled();

        logger.debug("Feed provider: systemId={}, enabled={}", systemId, enabled);

        // Only include providers that are enabled
        if (Boolean.FALSE.equals(enabled)) {
          logger.debug("Excluding {} because it is disabled", systemId);
          return false;
        }

        logger.debug("Including {} in system discovery", systemId);
        return true;
      })
      .map(systemDiscoveryMapper::mapSystemDiscovery)
      .toList();

    logger.debug("Filtered to {} systems for discovery", filteredSystems.size());
    mappedSystemDiscovery.setSystems(filteredSystems);
    return mappedSystemDiscovery;
  }

  public GBFSManifest mapGBFSManifest(SystemDiscovery mappedSystemDiscovery) {
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
                        .withUrl(baseUrl + "/gbfs/v3/" + system.getId() + "/gbfs")
                    )
                  )
              )
              .toList()
          )
      );
  }
}
