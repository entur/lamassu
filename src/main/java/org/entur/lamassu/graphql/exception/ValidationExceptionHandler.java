package org.entur.lamassu.graphql.exception;

import graphql.ErrorType;
import graphql.GraphQLError;
import org.springframework.graphql.data.method.annotation.GraphQlExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ValidationExceptionHandler {

  @GraphQlExceptionHandler(IllegalArgumentException.class)
  protected GraphQLError handleIllegalArgumentException(IllegalArgumentException ex) {
    return GraphQLError
      .newError()
      .errorType(ErrorType.ValidationError)
      .message(ex.getMessage())
      .build();
  }
}
