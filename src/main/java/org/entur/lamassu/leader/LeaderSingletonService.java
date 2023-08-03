package org.entur.lamassu.leader;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.entur.lamassu.service.GeoSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Profile("leader")
public class LeaderSingletonService {

  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private final FeedUpdater feedUpdater;
  private final GeoSearchService geoSearchService;

  public LeaderSingletonService(
    @Autowired FeedUpdater feedUpdater,
    @Autowired GeoSearchService geoSearchService
  ) {
    this.feedUpdater = feedUpdater;
    this.geoSearchService = geoSearchService;
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
}
