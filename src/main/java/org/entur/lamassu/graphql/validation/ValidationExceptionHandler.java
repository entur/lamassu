package org.entur.lamassu.graphql.validation;

import graphql.ErrorType;
import graphql.GraphQLError;
import graphql.GraphqlErrorException;
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

  /**
   * QueryParameterValidator raises GraphqlErrorException for unknown codespaces and systems.
   * Without this handler it is not resolved as a GraphQL error and clients get an opaque
   * INTERNAL_ERROR instead of the validation message.
   */
  @GraphQlExceptionHandler(GraphqlErrorException.class)
  protected GraphQLError handleGraphqlErrorException(GraphqlErrorException ex) {
    return GraphQLError
      .newError()
      .errorType(ErrorType.ValidationError)
      .message(ex.getMessage())
      .build();
  }
}
