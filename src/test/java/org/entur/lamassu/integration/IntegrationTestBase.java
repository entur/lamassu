package org.entur.lamassu.integration;

import org.entur.lamassu.config.TestRedisConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = TestRedisConfiguration.class)
public abstract class IntegrationTestBase {
}
