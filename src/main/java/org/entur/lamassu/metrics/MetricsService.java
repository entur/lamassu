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

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.entur.gbfs.validation.model.ValidationResult;
import org.entur.lamassu.model.provider.FeedProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class MetricsService {

  private static final String VALIDATION_MISSING_REQUIRED_FILE =
    "app.lamassu.gbfs.validation.missingrequiredfile";
  private static final String VALIDATION_FILE_ERRORS =
    "app.lamassu.gbfs.validation.fileerrors";
  private static final String VALIDATION_FEED_ERRORS =
    "app.lamassu.gbfs.validation.feederrors";
  public static final String LABEL_SYSTEM = "system";
  public static final String LABEL_VERSION = "version";
  public static final String LABEL_FILE = "file";

  public static final String SUBSCRIPTION_FAILEDSETUP =
    "app.lamassu.gbfs.subscription.failedsetup";
  public static final String LABEL_ENTITY = "entity";

  public static final String ENTITY_VEHICLE = "vehicle";
  public static final String ENTITY_STATION = "station";

  private final MeterRegistry meterRegistry;

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  private final AtomicInteger vehicleEntityCount = new AtomicInteger();
  private final AtomicInteger stationEntityCount = new AtomicInteger();

  public MetricsService(MeterRegistry meterRegistry) {
    this.meterRegistry = meterRegistry;
    Gauge
            .builder(
                    "app.lamassu.entity.count",
                    vehicleEntityCount,
                    AtomicInteger::doubleValue
            )
            .strongReference(true)
            .tags(List.of(Tag.of(LABEL_ENTITY, ENTITY_VEHICLE)))
            .register(meterRegistry);

    Gauge
            .builder(
                    "app.lamassu.entity.count",
                    stationEntityCount,
                    AtomicInteger::doubleValue
            )
            .strongReference(true)
            .tags(List.of(Tag.of(LABEL_ENTITY, ENTITY_STATION)))
            .register(meterRegistry);
  }

  public void registerSubscriptionSetup(FeedProvider feedProvider, boolean success) {
    meterRegistry.gauge(
      SUBSCRIPTION_FAILEDSETUP,
      List.of(Tag.of(LABEL_SYSTEM, feedProvider.getSystemId())),
      success ? 0 : 1
    );
  }

  public void registerValidationResult(
    FeedProvider feedProvider,
    ValidationResult validationResult
  ) {
    meterRegistry.gauge(
      VALIDATION_FEED_ERRORS,
      List.of(
        Tag.of(LABEL_SYSTEM, feedProvider.getSystemId()),
        Tag.of(LABEL_VERSION, validationResult.getSummary().getVersion())
      ),
      validationResult.getSummary().getErrorsCount()
    );

    validationResult
      .getFiles()
      .forEach((file, result) -> {
        if (result.isRequired()) {
          meterRegistry.gauge(
            VALIDATION_MISSING_REQUIRED_FILE,
            List.of(
              Tag.of(LABEL_SYSTEM, feedProvider.getSystemId()),
              Tag.of(LABEL_VERSION, result.getVersion()),
              Tag.of(LABEL_FILE, file)
            ),
            result.isExists() ? 0 : 1
          );
        }

        if (result.isExists()) {
          meterRegistry.gauge(
            VALIDATION_FILE_ERRORS,
            List.of(
              Tag.of(LABEL_SYSTEM, feedProvider.getSystemId()),
              Tag.of(LABEL_VERSION, result.getVersion()),
              Tag.of(LABEL_FILE, file)
            ),
            result.getErrorsCount()
          );
        }
      });
  }

  public void registerEntityCount(String entity, int entityCount) {
    if (entity.equals(ENTITY_STATION)) {
      this.stationEntityCount.set(entityCount);
    } else if (entity.equals(ENTITY_VEHICLE)) {
      this.vehicleEntityCount.set(entityCount);
    } else {
      logger.warn("entity unknown entity={}", entity);
    }
  }
}
