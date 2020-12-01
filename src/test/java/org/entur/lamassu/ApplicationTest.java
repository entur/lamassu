package org.entur.lamassu;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.entur.lamassu.config.TestRedisConfiguration;
import org.entur.lamassu.updater.ClusterSingletonService;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestRedisConfiguration.class, properties = "scheduling.enabled=false")
@AutoConfigureMockMvc
public class ApplicationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ClusterSingletonService clusterSingletonService;

    private final CountDownLatch waiter = new CountDownLatch(1);

    private static final MockWebServer mockWebServer = new MockWebServer();

    @BeforeClass
    public static void setUp() throws IOException, URISyntaxException, InterruptedException {
        var file = getFileFromResource("gbfs.json");
        var mockedResponse = Files.readString(Path.of(file.getPath()));
        mockWebServer.start(8888);
        mockWebServer.enqueue(
                new MockResponse()
                        .setBody(mockedResponse)
                        .addHeader("Content-Type", "application/json")
        );
    }

    @AfterClass
    public static void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    public void test() throws Exception {
        clusterSingletonService.heartbeat();

        // How can we wait until we know cache has been populated?
        waiter.await(1000, TimeUnit.MILLISECONDS);

        mockMvc.perform(get("/gbfs/tst/atlantis/rover/gbfs")
                .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.last_updated").value(1606727710));
    }

    private static File getFileFromResource(String fileName) throws URISyntaxException {
        ClassLoader classLoader = ApplicationTest.class.getClassLoader();
        URL resource = classLoader.getResource(fileName);
        if (resource == null) {
            throw new IllegalArgumentException("file not found! " + fileName);
        } else {
            return new File(resource.toURI());
        }
    }
}
