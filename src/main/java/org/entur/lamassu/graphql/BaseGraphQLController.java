package org.entur.lamassu.graphql.controller;

import graphql.ErrorType;
import graphql.GraphQLError;
import org.springframework.graphql.data.method.annotation.GraphQlExceptionHandler;

public abstract class BaseGraphQLController {

  @GraphQlExceptionHandler(IllegalArgumentException.class)
  protected GraphQLError handleIllegalArgumentException(IllegalArgumentException ex) {
    return GraphQLError
      .newError()
      .errorType(ErrorType.ValidationError)
      .message(ex.getMessage())
      .build();
  }
}
