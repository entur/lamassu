package org.entur.lamassu.config;

import org.entur.lamassu.model.gbfs.v2_1.GBFSBase;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.redisson.jcache.configuration.RedissonConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.cache.Cache;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;

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

    public Config getConfig() {
        return config;
    }

    @Bean
    public Cache<String, GBFSBase> feedCache() {
        var feedCacheConfig = new MutableConfiguration<String, GBFSBase>();
        var redissonFeedCacheConfig = RedissonConfiguration.fromConfig(config, feedCacheConfig);
        var manager = Caching.getCachingProvider().getCacheManager();
        return manager.createCache("gbfsCache", redissonFeedCacheConfig);
    }
}
