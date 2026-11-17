package org.entur.lamassu.metrics;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.entur.lamassu.cache.EntityCache;
import org.entur.lamassu.config.feedprovider.FeedProviderConfig;
import org.entur.lamassu.model.entities.System;
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
  private final EntityCache<System> systemCache;

  /**
   * The systems that were last reported as missing their system entry. Instance-local, so a
   * leader failover or restart reports the ones still missing again, as with duplicate ids.
   */
  private final Set<String> systemsReportedMissing = ConcurrentHashMap.newKeySet();

  /**
   * The last logged set of {@link DuplicateId} records per (codespace, entityType)
   * pair, keyed by {@code codespace + "/" + entityType}. Comparing full records
   * (not just ids) is what lets a change in which systems claim an already-known
   * duplicate id re-trigger a WARN with the newly correct operator ids.
   *
   * <p>This is instance-local state: it is not persisted or shared across replicas,
   * so a leader failover or restart forgets it, and the first tick afterwards
   * re-emits a WARN for every collision that is still unresolved. That is accepted
   * because failovers are rare relative to the 60-second detection tick, and the
   * cost of a spurious repeat log is far lower than the cost of staying silent.
   *
   * <p>It is a {@link ConcurrentHashMap} even though only the single scheduled
   * caller mutates or reads it today, purely as a defensive habit for a mutable
   * field on a singleton {@code @Component} bean: nothing here relies on
   * concurrent access, but nothing here would need to change if a second caller
   * were ever added.
   */
  private final Map<String, Set<DuplicateId>> lastReportedDuplicateIds =
    new ConcurrentHashMap<>();

  @Autowired
  public MetricUpdater(
    MetricsService metricsService,
    FeedProviderConfig feedProviderConfig,
    FeedFreshnessService feedFreshnessService,
    DuplicateIdService duplicateIdService,
    EntityCache<System> systemCache
  ) {
    this.metricsService = metricsService;
    this.feedProviderConfig = feedProviderConfig;
    this.feedFreshnessService = feedFreshnessService;
    this.duplicateIdService = duplicateIdService;
    this.systemCache = systemCache;
  }

  public void updateOutdatedFeedMetrics() {
    feedProviderConfig
      .getProviders()
      .parallelStream()
      .forEach(this::updateOutdatedFeedMetrics);
  }

  /**
   * Publishes, per provider, whether it is delivering data without having a system entry,
   * which is what leaves its vehicles and stations out of query results. That happens when
   * the provider stops serving a usable system_information file: the entry expires a day
   * after the last good one, while the feed itself keeps updating.
   *
   * <p>Logs a warning when a provider enters that state and a note when it leaves it, not
   * on every tick. Only providers that are enabled and aggregated are considered, and only
   * while their realtime feeds are fresh, so that a provider that is starting up or down
   * is not reported.
   */
  public void updateMissingSystemMetrics() {
    for (FeedProvider feedProvider : feedProviderConfig.getProviders()) {
      try {
        boolean missing = isMissingSystem(feedProvider);
        metricsService.registerSystemMissing(feedProvider, missing);
        logSystemMissingTransition(feedProvider, missing);
      } catch (RuntimeException e) {
        logger.warn(
          "Failed checking for a missing system entry for systemId={}",
          feedProvider.getSystemId(),
          e
        );
      }
    }
  }

  private boolean isMissingSystem(FeedProvider feedProvider) {
    return (
      Boolean.TRUE.equals(feedProvider.getEnabled()) &&
      Boolean.TRUE.equals(feedProvider.getAggregate()) &&
      feedFreshnessService.isLive(feedProvider) &&
      !systemCache.hasKey(feedProvider.getSystemId())
    );
  }

  private void logSystemMissingTransition(FeedProvider feedProvider, boolean missing) {
    String systemId = feedProvider.getSystemId();
    if (missing && systemsReportedMissing.add(systemId)) {
      logger.warn(
        "No system entry for systemId={} operatorId={} although its feeds are being updated, so its vehicles and stations are left out of query results. Its system_information in the feed cache was last updated {}. Check that the provider serves a valid system_information file.",
        systemId,
        feedProvider.getOperatorId(),
        feedFreshnessService
          .lastUpdated(feedProvider, GBFSFeed.Name.SYSTEM_INFORMATION)
          .map(Object::toString)
          .orElse("never")
      );
    } else if (!missing && systemsReportedMissing.remove(systemId)) {
      logger.info("System entry is back for systemId={}", systemId);
    }
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
    Set<DuplicateId> current = new LinkedHashSet<>(report.duplicates());
    Set<DuplicateId> previous = lastReportedDuplicateIds.getOrDefault(key, Set.of());

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
      "Duplicate ids across systems in codespace={} entity={} count={} {}",
      report.codespace(),
      report.entityType(),
      current.size(),
      describeDuplicates(report)
    );
  }

  private String describeDuplicates(DuplicateIdReport report) {
    Map<String, String> operatorIdsBySystemId = feedProviderConfig
      .getProviders()
      .stream()
      .filter(provider ->
        provider.getSystemId() != null && provider.getOperatorId() != null
      )
      .collect(
        Collectors.toMap(
          FeedProvider::getSystemId,
          FeedProvider::getOperatorId,
          (first, second) -> first
        )
      );

    return report
      .duplicates()
      .stream()
      .map(duplicate ->
        duplicate.id() +
        "=" +
        describeSystems(duplicate.systemIds(), operatorIdsBySystemId)
      )
      .collect(Collectors.joining("; "));
  }

  private String describeSystems(
    Set<String> systemIds,
    Map<String, String> operatorIdsBySystemId
  ) {
    return systemIds
      .stream()
      .map(systemId -> {
        String operatorId = operatorIdsBySystemId.getOrDefault(systemId, "unknown");
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
