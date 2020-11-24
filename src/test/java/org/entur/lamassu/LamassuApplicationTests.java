package org.entur.lamassu;

import org.entur.lamassu.config.TestRedisConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = TestRedisConfiguration.class)
class LamassuApplicationTests {

    @Test
    void contextLoads() {
    }

}
