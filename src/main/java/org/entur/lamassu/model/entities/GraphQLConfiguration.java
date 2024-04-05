package org.entur.lamassu.model.entities;

import graphql.schema.GraphQLScalarType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GraphQLConfiguration {

  @Bean
  public GraphQLScalarType polylineEncodedRingScalar() {
    return GraphQLScalarType
      .newScalar()
      .name("PolylineEncodedRing")
      .description("Custom scalar for handling polyline encoded rings")
      .coercing(new PolylineEncodedRingCoercing())
      .build();
  }
}
