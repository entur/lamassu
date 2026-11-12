/*
 *
 *
 *  * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 *  * the European Commission - subsequent versions of the EUPL (the "Licence");
 *  * You may not use this work except in compliance with the Licence.
 *  * You may obtain a copy of the Licence at:
 *  *
 *  *   https://joinup.ec.europa.eu/software/page/eupl
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the Licence is distributed on an "AS IS" basis,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the Licence for the specific language governing permissions and
 *  * limitations under the Licence.
 *
 */

package org.entur.lamassu.config.feedprovider;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

/**
 * {@link FeedProviderConfigRedis} uses Redisson's JsonJacksonCodec, which needs the Jackson 2
 * databind at runtime. Spring Boot 4 and the GBFS libraries only bring Jackson 3, and the test
 * classpath has Jackson 2 through test-scoped dependencies, so a missing runtime dependency is
 * invisible to the other tests. This test loads the codec from the runtime classpath only
 * (written by maven-dependency-plugin to target/runtime-classpath.txt).
 */
class RedissonJacksonCodecRuntimeClasspathTest {

  private static final Path RUNTIME_CLASSPATH = Path.of(
    "target",
    "runtime-classpath.txt"
  );

  @Test
  void jsonJacksonCodecCanBeCreatedWithRuntimeClasspathOnly() throws IOException {
    assertTrue(
      Files.exists(RUNTIME_CLASSPATH),
      RUNTIME_CLASSPATH + " is missing, it is written by maven-dependency-plugin"
    );

    try (
      var runtimeOnly = new URLClassLoader(
        runtimeClasspath(),
        ClassLoader.getPlatformClassLoader()
      )
    ) {
      assertDoesNotThrow(() ->
        Class
          .forName("org.redisson.codec.JsonJacksonCodec", true, runtimeOnly)
          .getDeclaredConstructor()
          .newInstance()
      );
    }
  }

  private static URL[] runtimeClasspath() throws IOException {
    return Arrays
      .stream(Files.readString(RUNTIME_CLASSPATH).trim().split(File.pathSeparator))
      .map(entry -> toUrl(Path.of(entry)))
      .toArray(URL[]::new);
  }

  private static URL toUrl(Path path) {
    try {
      return path.toUri().toURL();
    } catch (MalformedURLException e) {
      throw new IllegalStateException(e);
    }
  }
}
