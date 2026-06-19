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

package org.entur.lamassu.config;

import java.util.TimeZone;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.util.StdDateFormat;

@Configuration
public class JacksonConfig {

  /**
   * Render {@link java.util.Date} fields (e.g. the GBFS v3 {@code last_updated} timestamps) as
   * RFC3339 with an explicit numeric UTC offset ({@code +00:00}).
   *
   * <p>Jackson 3 (Spring Boot 4) changed the default {@link StdDateFormat} behaviour to collapse a
   * zero offset to a {@code Z} suffix. Both forms are valid RFC3339, but emitting {@code +00:00}
   * preserves the exact pre-upgrade GBFS feed output for downstream consumers. This cannot be
   * expressed via {@code spring.jackson.date-format}, since a {@code SimpleDateFormat} pattern also
   * renders UTC as {@code Z}.
   */
  @Bean
  JsonMapperBuilderCustomizer rfc3339NumericOffsetDateFormat() {
    return builder ->
      builder.defaultDateFormat(
        new StdDateFormat()
          .withTimeZone(TimeZone.getTimeZone("UTC"))
          .withZeroOffsetAsZ(false)
      );
  }
}
