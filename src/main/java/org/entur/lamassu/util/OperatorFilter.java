package org.entur.lamassu.util;

import java.util.List;
import java.util.Locale;
import org.entur.lamassu.model.entities.FormFactor;
import org.entur.lamassu.model.provider.FeedProvider;

public class OperatorFilter {

  private final List<FormFactor> formFactors;

  public OperatorFilter(List<FormFactor> formFactors) {
    this.formFactors = formFactors;
  }

  public boolean matches(FeedProvider feedProvider) {
    return this.hasFormFactor(feedProvider);
  }

  public boolean hasFormFactor(FeedProvider feedProvider) {
    if (
      this.formFactors != null &&
      !this.formFactors.isEmpty() &&
      feedProvider
        .getVehicleTypes()
        .stream()
        .noneMatch(vt ->
          this.formFactors.contains(FormFactor.valueOf(vt.getFormFactor().name()))
        )
    ) {
      return false;
    }
    return true;
  }
}
