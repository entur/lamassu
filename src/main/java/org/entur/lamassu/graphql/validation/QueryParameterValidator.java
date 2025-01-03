package org.entur.lamassu.graphql.validation;

import graphql.GraphqlErrorException;
import java.util.List;
import java.util.function.Predicate;
import org.entur.lamassu.service.FeedProviderService;
import org.springframework.stereotype.Service;

@Service
public class QueryParameterValidator {

  private final FeedProviderService feedProviderService;

  public QueryParameterValidator(FeedProviderService feedProviderService) {
    this.feedProviderService = feedProviderService;
  }

  public void validateCount(Integer count) {
    if (count != null) {
      validate(p -> p > 0, count, "Count must be positive");
    }
  }

  public void validateRange(Double range) {
    validate(p -> p > -1, range, "Range must be non-negative");
  }

  public void validateCodespaces(List<String> codespaces) {
    if (codespaces != null) {
      var validCodespaces = feedProviderService.getCodespaces();
      validate(validCodespaces::containsAll, codespaces, "Unknown codespace(s)");
    }
  }

  public void validateSystems(List<String> systems) {
    if (systems != null) {
      var validSystems = feedProviderService.getSystems();
      validate(validSystems::containsAll, systems, "Unknown system(s)");
    }
  }

  public void validateQueryParameters(
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

  public boolean isRangeSearch(Double range, Double lat, Double lon) {
    return range != null && lat != null && lon != null;
  }

  private boolean isBoundingBoxSearch(
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

  private <T> void validate(Predicate<T> predicate, T value, String message) {
    if (predicate.negate().test(value)) {
      throw new GraphqlErrorException.Builder().message(message).build();
    }
  }
}
