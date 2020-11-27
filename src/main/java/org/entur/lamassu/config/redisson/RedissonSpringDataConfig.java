package org.entur.lamassu.config.redisson;

import org.entur.lamassu.config.redisson.RedissonConfig;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.spring.data.connection.RedissonConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonSpringDataConfig {

    @Bean
    public RedissonConnectionFactory redissonConnectionFactory(RedissonClient redisson) {
        return new RedissonConnectionFactory(redisson);
    }

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redisson(RedissonConfig redissonConfig) {
        return Redisson.create(redissonConfig.getConfig());
    }
}

