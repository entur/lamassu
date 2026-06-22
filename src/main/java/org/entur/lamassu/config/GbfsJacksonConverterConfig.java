/*
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *   https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */

package org.entur.lamassu.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import java.util.List;
import java.util.TimeZone;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Serializes GBFS feed responses (the {@code org.mobilitydata.gbfs.*} model tree) with a Jackson 2
 * {@code ObjectMapper}, while leaving every other endpoint on the Spring Boot 4 default (Jackson 3).
 *
 * <p>Spring Boot 4 switched the default HTTP message converter to Jackson 3, whose packages were
 * renamed from {@code com.fasterxml.jackson} to {@code tools.jackson}. The GBFS model classes — and
 * in particular the GeoJSON geometry types from the {@code geojson-jackson} library
 * ({@code org.geojson.LngLatAlt}) — carry Jackson 2 annotations such as
 * {@code @JsonSerialize(using = LngLatAltSerializer.class)}. Jackson 3 does not recognise Jackson 2
 * annotations, so the custom GeoJSON serializer is silently ignored and coordinates are emitted as
 * bean objects ({@code {longitude, latitude, altitude, additionalElements}}) instead of compact
 * {@code [lon, lat]} arrays — producing invalid GBFS geofencing zone output.
 *
 * <p>This is an immediate, narrowly scoped fix: a Jackson 2 converter is inserted ahead of the
 * default converters but restricted to GBFS model types, so only those responses are routed through
 * Jackson 2. A longer-term fix (mapping geometry to a Jackson-3-friendly model, or registering a
 * Jackson 3 serializer for {@code LngLatAlt}) should remove the dependency on Jackson 2 entirely.
 */
@Configuration
public class GbfsJacksonConverterConfig implements WebMvcConfigurer {

  private static final String GBFS_MODEL_PACKAGE_PREFIX = "org.mobilitydata.gbfs.";

  @Override
  public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
    converters.add(0, gbfsJackson2Converter());
  }

  private static MappingJackson2HttpMessageConverter gbfsJackson2Converter() {
    return new MappingJackson2HttpMessageConverter(gbfsObjectMapper()) {
      @Override
      public boolean canWrite(Class<?> clazz, MediaType mediaType) {
        return isGbfsModel(clazz) && super.canWrite(clazz, mediaType);
      }
    };
  }

  /**
   * Builds a Jackson 2 ObjectMapper that reproduces the pre-Spring-Boot-4 GBFS feed serialization.
   *
   * <p>The date-format configuration only affects GBFS v3 feeds, whose timestamps (e.g.
   * {@code last_updated}) are {@link java.util.Date} fields. These are rendered as RFC3339 strings
   * (not numeric timestamps) with an explicit numeric UTC offset ({@code +00:00}), matching
   * {@link JacksonConfig} which applies the equivalent setting to the Jackson 3 mapper used by the
   * rest of the application. GBFS v2 timestamps are plain integer epoch-second fields and are
   * unaffected by these settings.
   */
  private static ObjectMapper gbfsObjectMapper() {
    return Jackson2ObjectMapperBuilder
      .json()
      .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
      .dateFormat(
        new StdDateFormat()
          .withTimeZone(TimeZone.getTimeZone("UTC"))
          .withColonInTimeZone(true)
      )
      .build();
  }

  private static boolean isGbfsModel(Class<?> clazz) {
    return clazz.getName().startsWith(GBFS_MODEL_PACKAGE_PREFIX);
  }
}
