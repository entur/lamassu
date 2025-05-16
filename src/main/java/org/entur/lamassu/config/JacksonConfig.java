package org.entur.lamassu.config;

import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.entur.gbfs.authentication.RequestAuthenticator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * Jackson configuration for the application.
 * This configures the global ObjectMapper with custom settings for serialization.
 */
@Configuration
public class JacksonConfig {

  /**
   * Mark RequestAuthenticator for Jackson to ignore during serialization.
   * This prevents serialization errors with implementations like Oauth2ClientCredentialsGrantRequestAuthenticator.
   */
  @JsonIgnoreType
  public abstract static class MixInForRequestAuthenticator
    implements RequestAuthenticator {}

  /**
   * Configures the global ObjectMapper with custom settings.
   * Uses Jackson2ObjectMapperBuilder to avoid circular dependencies.
   *
   * @return A configured ObjectMapper
   */
  @Bean
  @Primary
  public ObjectMapper objectMapper() {
    return Jackson2ObjectMapperBuilder
      .json()
      .mixIn(RequestAuthenticator.class, MixInForRequestAuthenticator.class)
      .featuresToDisable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
      .build();
  }
}
