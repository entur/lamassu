package org.entur.lamassu.metrics;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.entur.lamassu.config.feedprovider.FeedProviderConfig;
import org.entur.lamassu.model.provider.FeedProvider;
import org.entur.lamassu.service.DuplicateIdService;
import org.entur.lamassu.service.DuplicateIdService.DuplicateId;
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

  private final Map<String, Set<String>> lastReportedDuplicateIds =
    new ConcurrentHashMap<>();

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
   *
   * <p>Also logs a drill-down of which ids collide and which systems (and their
   * operator ids) claim them, but only when that set changes from the previous tick.
   * This runs on a fixed schedule, so logging unconditionally would repeat the same
   * message forever; logging only on change keeps it actionable.
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
        logDuplicateIdTransition(report);
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

  private void logDuplicateIdTransition(DuplicateIdReport report) {
    String key = report.codespace() + "/" + report.entityType();
    Set<String> current = report
      .duplicates()
      .stream()
      .map(DuplicateId::id)
      .collect(Collectors.toCollection(LinkedHashSet::new));
    Set<String> previous = lastReportedDuplicateIds.getOrDefault(key, Set.of());

    if (current.equals(previous)) {
      return;
    }

    if (current.isEmpty()) {
      lastReportedDuplicateIds.remove(key);
      logger.info(
        "Duplicate ids resolved in codespace={} entity={}",
        report.codespace(),
        report.entityType()
      );
      return;
    }

    lastReportedDuplicateIds.put(key, current);
    logger.warn(
      "Duplicate ids across systems in codespace={} entity={} count={}{}",
      report.codespace(),
      report.entityType(),
      current.size(),
      describeDuplicates(report)
    );
  }

  private String describeDuplicates(DuplicateIdReport report) {
    return report
      .duplicates()
      .stream()
      .map(duplicate ->
        String.format(
          "%n      %s claimed by %s",
          duplicate.id(),
          describeSystems(duplicate.systemIds())
        )
      )
      .collect(Collectors.joining());
  }

  private String describeSystems(Set<String> systemIds) {
    return systemIds
      .stream()
      .map(systemId -> {
        FeedProvider provider = feedProviderConfig.getProviderBySystemId(systemId);
        String operatorId = provider == null ? "unknown" : provider.getOperatorId();
        return systemId + "(" + operatorId + ")";
      })
      .collect(Collectors.joining(", ", "[", "]"));
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
