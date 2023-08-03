package org.entur.lamassu.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import redis.embedded.RedisServer;

@Configuration
public class TestRedisConfiguration {

  @Bean(destroyMethod = "shutdown")
  @DependsOn("redissonServer")
  public RedissonClient redissonClient(Config redissonConfig) {
    return Redisson.create(redissonConfig);
  }

  @Bean(initMethod = "start", destroyMethod = "stop")
  public RedisServer redissonServer(RedisProperties redisProperties) {
    return new RedisServer(redisProperties.getPort());
  }
}
