package org.entur.lamassu.updater;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class ClusterSingletonService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private RLock lock;
    private FeedUpdateScheduler feedUpdateScheduler;
    private boolean isLeader = false;

    public ClusterSingletonService(RedissonClient redisson, @Autowired FeedUpdateScheduler feedUpdateScheduler) {
        this.lock = redisson.getLock("leader");
        this.feedUpdateScheduler = feedUpdateScheduler;
    }

    /**
     * Check leadership status every 30 seconds
     *
     * If we are currently the leader, renew leadership lease and if that fails, stop scheduling updates
     *
     * If we are not currently the leader, try to become leader, and if that succeeds, start scheduling updates.
     *
     * Leadership lease time is 60 seconds.
     */
    @Scheduled(fixedRate = 30000)
    public void heartbeat() {
        if (isLeader()) {
            logger.info("I am already the leader. Will try to renew.");
            if (tryToBecomeLeader()) {
                logger.info("Leadership renewed.");
            } else {
                logger.info("Lost leadership");
                isLeader = false;
                feedUpdateScheduler.stop();
            }
        } else {
            logger.info("Trying to become leader.");
            if (tryToBecomeLeader()) {
                logger.info("I became the leader");
                isLeader = true;
                feedUpdateScheduler.start();
            } else {
                logger.info("Sorry, someone else is the leader, try again soon");
            }
        }
    }

    private boolean isLeader() {
        return lock.isHeldByCurrentThread() || isLeader;
    }

    private boolean tryToBecomeLeader() {
        try {
            return lock.tryLock(1, 60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            return false;
        }
    }
}
