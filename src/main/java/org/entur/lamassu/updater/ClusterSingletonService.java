package org.entur.lamassu.updater;

import org.entur.lamassu.service.GeoSearchService;
import org.redisson.api.RLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class ClusterSingletonService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final RLock feedUpdateSchedulerLock;
    private final FeedUpdater feedUpdater;
    private final ListenerManager listenerManager;
    private final GeoSearchService geoSearchService;

    private boolean isLeader = false;

    public ClusterSingletonService(RLock feedUpdateSchedulerLock, @Autowired FeedUpdater feedUpdater, @Autowired ListenerManager listenerManager, @Autowired GeoSearchService geoSearchService) {
        this.feedUpdateSchedulerLock = feedUpdateSchedulerLock;
        this.feedUpdater = feedUpdater;
        this.listenerManager = listenerManager;
        this.geoSearchService = geoSearchService;
    }

    /**
     * Check leadership status every 15 seconds
     *
     * If we are currently the leader, renew leadership lease and if that fails, stop scheduling updates
     *
     * If we are not currently the leader, try to become leader, and if that succeeds, start scheduling updates.
     *
     * Leadership lease time is 60 seconds.
     */
    @Scheduled(fixedRate = 15000)
    public void heartbeat() throws InterruptedException {
        if (isLeader()) {
            logger.debug("I am already the leader. Will try to renew.");
            if (tryToBecomeLeader()) {
                logger.debug("Leadership renewed.");
            } else {
                logger.info("Lost leadership");
                isLeader = false;
                feedUpdater.stop();
                listenerManager.stop();
            }
        } else {
            logger.debug("Trying to become leader.");
            if (tryToBecomeLeader()) {
                logger.info("I became the leader");
                isLeader = true;
                feedUpdater.start();
                listenerManager.start();
            } else {
                logger.debug("Sorry, someone else is the leader, try again soon");
            }
        }
    }

    @Scheduled(fixedRateString = "${org.entur.lamassu.feedupdateinterval:30000}")
    public void update() {
        if (isLeader()) {
            feedUpdater.update();
        }
    }

    @Scheduled(fixedRate = 60000)
    public void removeOrphans() {
        if (isLeader()) {
            var removedOrphans = geoSearchService.removeVehicleSpatialIndexOrphans();
            if (removedOrphans.size() > 0) {
                logger.info("Removed {} orphans in vehicle spatial index", removedOrphans.size());
            }
        }
    }

    public boolean isLeader() {
        return feedUpdateSchedulerLock.isHeldByCurrentThread() || isLeader;
    }

    private boolean tryToBecomeLeader() throws InterruptedException {
        try {
            return feedUpdateSchedulerLock.tryLock(1, 60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            isLeader = false;
            feedUpdater.stop();
            listenerManager.stop();
            throw e;
        }
    }
}
