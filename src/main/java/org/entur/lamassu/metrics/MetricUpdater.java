package org.entur.lamassu.metrics;

import java.util.Arrays;
import org.entur.lamassu.config.feedprovider.FeedProviderConfig;
import org.entur.lamassu.model.provider.FeedProvider;
import org.entur.lamassu.service.FeedFreshnessService;
import org.mobilitydata.gbfs.v3_0.gbfs.GBFSFeed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MetricUpdater {

  private final MetricsService metricsService;
  private final FeedProviderConfig feedProviderConfig;
  private final FeedFreshnessService feedFreshnessService;

  @Autowired
  public MetricUpdater(
    MetricsService metricsService,
    FeedProviderConfig feedProviderConfig,
    FeedFreshnessService feedFreshnessService
  ) {
    this.metricsService = metricsService;
    this.feedProviderConfig = feedProviderConfig;
    this.feedFreshnessService = feedFreshnessService;
  }

  public void updateOutdatedFeedMetrics() {
    feedProviderConfig
      .getProviders()
      .parallelStream()
      .forEach(this::updateOutdatedFeedMetrics);
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
