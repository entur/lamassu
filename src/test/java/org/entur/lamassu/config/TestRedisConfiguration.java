package org.entur.lamassu.config;

import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.test.context.TestConfiguration;
import redis.embedded.RedisServer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@TestConfiguration
public class TestRedisConfiguration {

    private final RedisProperties redisProperties;
    private RedisServer redisServer;

    public TestRedisConfiguration(RedisProperties redisProperties) {
        this.redisProperties = redisProperties;
    }

    @PostConstruct
    public void postConstruct() {
        if (redisServer == null || !redisServer.isActive()) {
            redisServer = new RedisServer(redisProperties.getPort());
            redisServer.start();
        }
    }

    @PreDestroy
    public void preDestroy() {
        redisServer.stop();
    }
}
