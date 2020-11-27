package org.entur.lamassu.config.redisson;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.locks.Lock;

import static org.entur.lamassu.config.redisson.RedissonConstants.FEED_UPDATE_SCHEDULER_LOCK;

@Configuration
public class RedissonLockConfig {

    @Bean
    RLock feedUpdateSchedulerLock(RedissonClient redissonClient) {
        return redissonClient.getLock(FEED_UPDATE_SCHEDULER_LOCK);
    }
}
