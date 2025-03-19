package org.entur.lamassu.metrics;

import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import org.entur.lamassu.cache.GBFSV3FeedCache;
import org.entur.lamassu.config.feedprovider.FeedProviderConfig;
import org.entur.lamassu.model.provider.FeedProvider;
import org.mobilitydata.gbfs.v3_0.gbfs.GBFSFeed;
import org.mobilitydata.gbfs.v3_0.gbfs.GBFSFeedName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MetricUpdater {

  @Value("${org.entur.lamassu.max-tolerated-overdue-seconds:120}")
  private Integer maxToleratedOverdueSeconds = 120;

  private final MetricsService metricsService;
  private final FeedProviderConfig feedProviderConfig;
  private final GBFSV3FeedCache feedCache;

  @Autowired
  public MetricUpdater(
    MetricsService metricsService,
    FeedProviderConfig feedProviderConfig,
    GBFSV3FeedCache feedCache
  ) {
    this.metricsService = metricsService;
    this.feedProviderConfig = feedProviderConfig;
    this.feedCache = feedCache;
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
      .mapToInt(feedName -> isFeedOverdue(feedProvider, feedName) ? 1 : 0)
      .sum();

    metricsService.registerOverdueFilesCount(feedProvider, overdueFilesCount);
  }

  private boolean isFeedOverdue(FeedProvider feedProvider, GBFSFeed.Name feedName) {
    Object feed = feedCache.find(feedName, feedProvider);
    if (feed != null) {
      long absoluteTtl = getAbsoluteTtl(GBFSFeedName.implementingClass(feedName), feed);
      return absoluteTtl + maxToleratedOverdueSeconds < Instant.now().getEpochSecond();
    }
    return false;
  }

  private <T> long getAbsoluteTtl(Class<?> feedClass, T feed) {
    try {
      Date lastUpdated = (Date) feedClass.getMethod("getLastUpdated").invoke(feed);
      Integer ttl = (Integer) feedClass.getMethod("getTtl").invoke(feed);
      return lastUpdated.getTime() / 1000 + ttl;
    } catch (
      NoSuchMethodException | InvocationTargetException | IllegalAccessException e
    ) {
      return 0;
    }
  }
}
