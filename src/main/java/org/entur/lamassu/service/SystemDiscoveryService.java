package org.entur.lamassu.service;

import org.entur.lamassu.model.discovery.SystemDiscovery;
import org.mobilitydata.gbfs.v3_0.manifest.GBFSManifest;

public interface SystemDiscoveryService {
  SystemDiscovery getSystemDiscovery();
  GBFSManifest getGBFSManifest();
}
