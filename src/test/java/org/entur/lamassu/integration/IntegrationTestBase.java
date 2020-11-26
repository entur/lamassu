package org.entur.lamassu.integration;

import org.entur.lamassu.config.TestRedisConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = TestRedisConfiguration.class, properties = "scheduling.enabled=false")
public abstract class IntegrationTestBase {
}
