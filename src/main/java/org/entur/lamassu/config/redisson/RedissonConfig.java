package org.entur.lamassu.config.redisson;

import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {
    private final Config config;

    public RedissonConfig(RedisProperties redisProperties) {
        config = new Config();
        config.setCodec(JsonJacksonCodec.INSTANCE);
        String address = String.format(
                "redis://%s:%s",
                redisProperties.getHost(),
                redisProperties.getPort()
        );
        config.useSingleServer()
                .setAddress(address);
    }

    @Bean
    public Config getConfig() {
        return config;
    }
}
