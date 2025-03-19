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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.entur.gbfs.validation.model.ValidationResult;
import org.entur.lamassu.model.provider.FeedProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class MetricsService {

  protected static final String VALIDATION_MISSING_REQUIRED_FILES =
    "app.lamassu.gbfs.validation.missingrequiredfiles";
  protected static final String VALIDATION_FEED_ERRORS =
    "app.lamassu.gbfs.validation.feederrors";
  public static final String LABEL_SYSTEM = "system";

  protected static final String SUBSCRIPTION_FAILEDSETUP =
    "app.lamassu.gbfs.subscription.failedsetup";
  public static final String FILES_OVERDUE = "app.lamassu.gbfs.filesoverdue";
  public static final String LABEL_ENTITY = "entity";

  public static final String ENTITY_VEHICLE = "vehicle";
  public static final String ENTITY_STATION = "station";

  private final MeterRegistry meterRegistry;

  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  private final AtomicInteger vehicleEntityCount = new AtomicInteger();
  private final AtomicInteger stationEntityCount = new AtomicInteger();

  private final Map<String, AtomicInteger> subscriptionFailedSetupCounters =
    new ConcurrentHashMap<>();
  private final Map<String, AtomicInteger> validationFeedErrorsCounters =
    new ConcurrentHashMap<>();
  private final Map<String, AtomicInteger> validationMissingRequiredFilesCounters =
    new ConcurrentHashMap<>();
  private final Map<String, AtomicInteger> overdueFilesCounters =
    new ConcurrentHashMap<>();

  public MetricsService(MeterRegistry meterRegistry) {
    this.meterRegistry = meterRegistry;
    Gauge
      .builder("app.lamassu.entity.count", vehicleEntityCount, AtomicInteger::doubleValue)
      .strongReference(true)
      .tags(List.of(Tag.of(LABEL_ENTITY, ENTITY_VEHICLE)))
      .register(meterRegistry);

    Gauge
      .builder("app.lamassu.entity.count", stationEntityCount, AtomicInteger::doubleValue)
      .strongReference(true)
      .tags(List.of(Tag.of(LABEL_ENTITY, ENTITY_STATION)))
      .register(meterRegistry);
  }

  public void registerSubscriptionSetup(FeedProvider feedProvider, boolean success) {
    getSubscriptionFailedSetupCounter(feedProvider).set(success ? 0 : 1);
  }

  public void registerValidationResult(
    FeedProvider feedProvider,
    ValidationResult validationResult
  ) {
    getValidationFeedErrorsCounter(feedProvider)
      .set(validationResult.summary().errorsCount());

    getMissingRequiredFilesCounter(feedProvider)
      .set(calculateMissingRequiredFiles(validationResult));
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

  public void registerOverdueFilesCount(
    FeedProvider feedProvider,
    int overdueFilesCount
  ) {
    getOverdueFilesCounter(feedProvider).set(overdueFilesCount);
  }

  private AtomicInteger getSubscriptionFailedSetupCounter(FeedProvider feedProvider) {
    return getCounter(
      feedProvider,
      subscriptionFailedSetupCounters,
      SUBSCRIPTION_FAILEDSETUP
    );
  }

  private AtomicInteger getMissingRequiredFilesCounter(FeedProvider feedProvider) {
    return getCounter(
      feedProvider,
      validationFeedErrorsCounters,
      VALIDATION_MISSING_REQUIRED_FILES
    );
  }

  private AtomicInteger getOverdueFilesCounter(FeedProvider feedProvider) {
    return getCounter(feedProvider, overdueFilesCounters, FILES_OVERDUE);
  }

  private AtomicInteger getValidationFeedErrorsCounter(FeedProvider feedProvider) {
    return getCounter(
      feedProvider,
      validationMissingRequiredFilesCounters,
      VALIDATION_FEED_ERRORS
    );
  }

  private AtomicInteger getCounter(
    FeedProvider feedProvider,
    Map<String, AtomicInteger> counterMap,
    String metricName
  ) {
    AtomicInteger counter;
    if (counterMap.containsKey(feedProvider.getSystemId())) {
      counter = counterMap.get(feedProvider.getSystemId());
    } else {
      counter = new AtomicInteger();
      counterMap.put(feedProvider.getSystemId(), counter);
      Gauge
        .builder(metricName, counter, AtomicInteger::doubleValue)
        .strongReference(true)
        .tags(List.of(Tag.of(LABEL_SYSTEM, feedProvider.getSystemId())))
        .register(meterRegistry);
    }
    return counter;
  }

  private int calculateMissingRequiredFiles(ValidationResult validationResult) {
    return validationResult
      .files()
      .values()
      .stream()
      .map(fileValidationResult -> {
        if (fileValidationResult.required() && !fileValidationResult.exists()) {
          return 1;
        } else {
          return 0;
        }
      })
      .reduce(0, Integer::sum);
  }
}
