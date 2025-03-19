package org.entur.lamassu.leader;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.entur.lamassu.metrics.MetricUpdater;
import org.entur.lamassu.service.GeoSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * This class orchestrates updates of GBFS feeds via the FeedUpdater, based on scheduling
 */
@Component
@Profile("leader")
public class LeaderSingletonService {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private final FeedUpdater feedUpdater;
  private final GeoSearchService geoSearchService;
  private final MetricUpdater metricUpdater;

  public LeaderSingletonService(
    @Autowired FeedUpdater feedUpdater,
    @Autowired GeoSearchService geoSearchService,
    @Autowired MetricUpdater metricUpdater
  ) {
    this.feedUpdater = feedUpdater;
    this.geoSearchService = geoSearchService;
    this.metricUpdater = metricUpdater;
  }

  @PostConstruct
  public void init() {
    logger.info("Initializing leader");
    feedUpdater.start();
  }

  @PreDestroy
  public void shutdown() {
    logger.info("Shutting down leader");
    feedUpdater.stop();
  }

  @Scheduled(fixedRateString = "${org.entur.lamassu.feedupdateinterval:30000}")
  public void update() {
    feedUpdater.update();
  }

  @Scheduled(fixedRate = 60000)
  public void removeOrphans() {
    var removedOrphans = geoSearchService.removeVehicleSpatialIndexOrphans();
    if (!removedOrphans.isEmpty()) {
      logger.info("Removed {} orphans in vehicle spatial index", removedOrphans.size());
    }
  }

  @Scheduled(fixedRateString = "${org.entur.lamassu.update-feed-metrics-interval:60000}")
  public void updateFeedMetrics() {
    metricUpdater.updateOutdatedFeedMetrics();
  }
}
