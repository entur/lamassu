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

import io.micrometer.core.instrument.Tag;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import org.entur.gbfs.validation.model.ValidationResult;
import org.entur.lamassu.model.provider.FeedProvider;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ValidationMetricService {
    private static final String VALIDATION_MISSING_REQUIRED_FILE = "app_lamassu_gbfs_validation_missingrequiredfile";
    private static final String VALIDATION_FILE_ERRORS = "app_lamassu_gbfs_validation_fileerrors";
    private static final String VALIDATION_FEED_ERRORS = "app_lamassu_gbfs_validation_feederrors";

    private final PrometheusMeterRegistry registry;

    public ValidationMetricService() {
        registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
    }

    public void registerValidationResult(FeedProvider feedProvider, ValidationResult validationResult) {
        registry.gauge(
                VALIDATION_FEED_ERRORS,
                List.of(
                        Tag.of("system", feedProvider.getSystemId()),
                        Tag.of("version", validationResult.getSummary().getVersion())
                ),
                validationResult.getSummary().getErrorsCount()
        );

        validationResult.getFiles().forEach((file, result) -> {
                    if (result.isRequired()) {
                        registry.gauge(
                                VALIDATION_MISSING_REQUIRED_FILE,
                                List.of(
                                        Tag.of("system", feedProvider.getSystemId()),
                                        Tag.of("version", result.getVersion()),
                                        Tag.of("file", file)
                                ),
                                result.isExists() ? 0 : 1
                        );
                    } else if (result.isExists()) {
                        registry.gauge(
                                VALIDATION_FILE_ERRORS,
                                List.of(
                                        Tag.of("system", feedProvider.getSystemId()),
                                        Tag.of("version", result.getVersion()),
                                        Tag.of("file", file)
                                ),
                                result.getErrorsCount()
                        );
                    }
                });
    }
}
