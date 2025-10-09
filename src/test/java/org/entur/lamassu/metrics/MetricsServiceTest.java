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

package org.entur.lamassu.metrics;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import org.entur.gbfs.validation.model.FileValidationResult;
import org.entur.gbfs.validation.model.ValidationResult;
import org.entur.gbfs.validation.model.ValidationSummary;
import org.entur.lamassu.model.provider.FeedProvider;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

class MetricsServiceTest {

  @Test
  void testRegisterSubscriptionSetup() {
    SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
    MetricsService metricsService = new MetricsService(meterRegistry);
    FeedProvider fp = new FeedProvider();
    fp.setSystemId("TestSystem");

    metricsService.registerSubscriptionSetup(fp, false);
    assertEquals(
      1.0,
      meterRegistry.get(MetricsService.SUBSCRIPTION_FAILEDSETUP).gauge().value(),
      0.01
    );

    metricsService.registerSubscriptionSetup(fp, true);
    assertEquals(
      0.0,
      meterRegistry.get(MetricsService.SUBSCRIPTION_FAILEDSETUP).gauge().value(),
      0.01
    );
  }

  @Test
  void testRegisterValidationResult() {
    SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
    MetricsService metricsService = new MetricsService(meterRegistry);
    FeedProvider fp = new FeedProvider();
    fp.setSystemId("TestSystem");

    metricsService.registerValidationResult(fp, getValidationResult(1));
    assertEquals(
      1.0,
      meterRegistry.get(MetricsService.VALIDATION_FEED_ERRORS).gauge().value(),
      0.01
    );

    metricsService.registerValidationResult(fp, getValidationResult(0));
    assertEquals(
      0.0,
      meterRegistry.get(MetricsService.VALIDATION_FEED_ERRORS).gauge().value(),
      0.01
    );
  }

  @Test
  void testRegisterValidationResultWithMissingRequiredFilse() {
    SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
    MetricsService metricsService = new MetricsService(meterRegistry);
    FeedProvider fp = new FeedProvider();
    fp.setSystemId("TestSystem");

    metricsService.registerValidationResult(
      fp,
      getValidationResultWithMissingRequiredFile()
    );
    assertEquals(
      1.0,
      meterRegistry.get(MetricsService.VALIDATION_FEED_ERRORS).gauge().value(),
      0.01
    );
    assertEquals(
      1.0,
      meterRegistry.get(MetricsService.VALIDATION_MISSING_REQUIRED_FILES).gauge().value(),
      0.01
    );

    metricsService.registerValidationResult(fp, getValidationResult(0));
    assertEquals(
      0.0,
      meterRegistry.get(MetricsService.VALIDATION_FEED_ERRORS).gauge().value(),
      0.01
    );
    assertEquals(
      0.0,
      meterRegistry.get(MetricsService.VALIDATION_MISSING_REQUIRED_FILES).gauge().value(),
      0.01
    );
  }

  private static @NotNull ValidationResult getValidationResult(int errorCount) {
    ValidationSummary validationSummary = new ValidationSummary(
      "3.0",
      Instant.now().getEpochSecond(),
      errorCount
    );

    FileValidationResult fileValidationResult = new FileValidationResult(
      "gbfs",
      true,
      true,
      errorCount,
      null,
      null,
      "3.0",
      Collections.emptyList(),
      Collections.emptyList()
    );

    return new ValidationResult(validationSummary, Map.of("gbfs", fileValidationResult));
  }

  private static @NotNull ValidationResult getValidationResultWithMissingRequiredFile() {
    ValidationSummary validationSummary = new ValidationSummary(
      "3.0",
      Instant.now().getEpochSecond(),
      1
    );

    /*String file, boolean required, boolean exists, int errorsCount, String schema, String fileContents, String version, List<FileValidationError> errors*/

    FileValidationResult fileValidationResult = new FileValidationResult(
      "gbfs",
      true,
      false,
      1,
      null,
      null,
      "3.0",
      Collections.emptyList(),
      Collections.emptyList()
    );

    return new ValidationResult(validationSummary, Map.of("gbfs", fileValidationResult));
  }
}
