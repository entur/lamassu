package org.entur.lamassu.metrics;

import java.util.Arrays;
import java.util.List;
import org.entur.lamassu.config.feedprovider.FeedProviderConfig;
import org.entur.lamassu.model.provider.FeedProvider;
import org.entur.lamassu.service.DuplicateIdService;
import org.entur.lamassu.service.DuplicateIdService.DuplicateIdReport;
import org.entur.lamassu.service.FeedFreshnessService;
import org.mobilitydata.gbfs.v3_0.gbfs.GBFSFeed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MetricUpdater {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  private final MetricsService metricsService;
  private final FeedProviderConfig feedProviderConfig;
  private final FeedFreshnessService feedFreshnessService;
  private final DuplicateIdService duplicateIdService;

  @Autowired
  public MetricUpdater(
    MetricsService metricsService,
    FeedProviderConfig feedProviderConfig,
    FeedFreshnessService feedFreshnessService,
    DuplicateIdService duplicateIdService
  ) {
    this.metricsService = metricsService;
    this.feedProviderConfig = feedProviderConfig;
    this.feedFreshnessService = feedFreshnessService;
    this.duplicateIdService = duplicateIdService;
  }

  public void updateOutdatedFeedMetrics() {
    feedProviderConfig
      .getProviders()
      .parallelStream()
      .forEach(this::updateOutdatedFeedMetrics);
  }

  /**
   * Publishes, per codespace and entity type, how many entity ids are claimed by more
   * than one system. Gauges are set on every tick, including to zero, so that a
   * resolved problem is visible.
   */
  public void updateDuplicateIdMetrics() {
    List<DuplicateIdReport> reports;
    try {
      reports = duplicateIdService.detect();
    } catch (RuntimeException e) {
      logger.warn("Failed detecting duplicate ids", e);
      return;
    }

    for (DuplicateIdReport report : reports) {
      try {
        metricsService.registerDuplicateIdCount(
          report.codespace(),
          report.entityType(),
          report.duplicates().size()
        );
      } catch (RuntimeException e) {
        logger.warn(
          "Failed registering duplicate id metric for codespace={} entity={}",
          report.codespace(),
          report.entityType(),
          e
        );
      }
    }
  }

  private void updateOutdatedFeedMetrics(FeedProvider feedProvider) {
    int overdueFilesCount = Arrays
      .asList(GBFSFeed.Name.values())
      .stream()
      // Since gbfs is not yet updated regularly, we skip it explicitly
      .filter(feedName -> !GBFSFeed.Name.GBFS.equals(feedName))
      .mapToInt(feedName ->
        feedFreshnessService.isFeedOverdue(feedProvider, feedName) ? 1 : 0
      )
      .sum();

    metricsService.registerOverdueFilesCount(feedProvider, overdueFilesCount);
  }
}
