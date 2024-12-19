package org.entur.lamassu.graphql.controller;

import graphql.ErrorType;
import graphql.GraphQLError;
import graphql.GraphqlErrorException;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.entur.lamassu.model.provider.FeedProvider;
import org.entur.lamassu.service.FeedProviderService;
import org.springframework.graphql.data.method.annotation.GraphQlExceptionHandler;

public abstract class BaseGraphQLController {

  private final FeedProviderService feedProviderService;

  protected BaseGraphQLController(FeedProviderService feedProviderService) {
    this.feedProviderService = feedProviderService;
  }

  @GraphQlExceptionHandler(IllegalArgumentException.class)
  protected GraphQLError handleIllegalArgumentException(IllegalArgumentException ex) {
    return GraphQLError
      .newError()
      .errorType(ErrorType.ValidationError)
      .message(ex.getMessage())
      .build();
  }

  protected void validateCount(Integer count) {
    if (count != null) {
      validate(p -> p > 0, count, "Count must be positive");
    }
  }

  protected void validateRange(Double range) {
    validate(p -> p > -1, range, "Range must be non-negative");
  }

  protected void validateCodespaces(List<String> codespaces) {
    if (codespaces != null) {
      var validCodespaces = getCodespaces();
      validate(validCodespaces::containsAll, codespaces, "Unknown codespace(s)");
    }
  }

  protected void validateSystems(List<String> systems) {
    if (systems != null) {
      var validSystems = getSystems();
      validate(validSystems::containsAll, systems, "Unknown system(s)");
    }
  }

  protected void validateQueryParameters(
    Double lat,
    Double lon,
    Double range,
    Double minimumLatitude,
    Double minimumLongitude,
    Double maximumLatitude,
    Double maximumLongitude
  ) {
    if (isRangeSearch(range, lat, lon)) {
      validateRange(range);
    } else if (
      !isBoundingBoxSearch(
        minimumLatitude,
        minimumLongitude,
        maximumLatitude,
        maximumLongitude
      )
    ) {
      throw new IllegalArgumentException(
        "At least one of minimumLatitude, minimumLongitude, maximumLatitude and maximumLongitude must be specified"
      );
    }
  }

  protected Collection<String> getSystems() {
    return feedProviderService
      .getFeedProviders()
      .stream()
      .map(FeedProvider::getSystemId)
      .collect(Collectors.toSet());
  }

  protected Collection<String> getCodespaces() {
    return feedProviderService
      .getFeedProviders()
      .stream()
      .map(FeedProvider::getCodespace)
      .collect(Collectors.toSet());
  }

  protected <T> void validate(Predicate<T> predicate, T value, String message) {
    if (predicate.negate().test(value)) {
      throw new GraphqlErrorException.Builder().message(message).build();
    }
  }

  protected boolean isRangeSearch(Double range, Double lat, Double lon) {
    return range != null && lat != null && lon != null;
  }

  protected boolean isBoundingBoxSearch(
    Double minimumLatitude,
    Double minimumLongitude,
    Double maximumLatitude,
    Double maximumLongitude
  ) {
    return (
      minimumLatitude != null &&
      minimumLongitude != null &&
      maximumLatitude != null &&
      maximumLongitude != null
    );
  }
}
