package org.entur.lamassu;

import org.entur.lamassu.LamassuApplication;
import org.entur.lamassu.config.cache.RedissonCacheConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

public class TestLamassuApplication extends LamassuApplication {
    public static void main(String[] args) {
        SpringApplication.run(TestLamassuApplication.class, args);
    }

}
