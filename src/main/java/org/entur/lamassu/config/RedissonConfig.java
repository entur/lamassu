package org.entur.lamassu.config;

import org.redisson.config.Config;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
public class RedissonConfig {
    private final Config config;

    public RedissonConfig(RedisProperties redisProperties) {
        config = new Config();
        String address = String.format(
                "redis://%s:%s",
                redisProperties.getHost(),
                redisProperties.getPort()
        );
        config.useSingleServer()
                .setAddress(address);
    }

    public Config getConfig() {
        return config;
    }
}
