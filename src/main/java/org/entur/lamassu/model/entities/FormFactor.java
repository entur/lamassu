package org.entur.lamassu.model.entities;

import java.util.ArrayList;
import java.util.List;

public enum FormFactor {
  BICYCLE,
  CARGO_BICYCLE,
  CAR,
  MOPED,
  SCOOTER,
  SCOOTER_STANDING,
  SCOOTER_SEATED,
  OTHER;

  /**
   * Expands a requested form-factor filter so the deprecated {@link #SCOOTER} value also matches
   * {@link #SCOOTER_STANDING}.
   *
   * <p>GBFS v3 split the v2 {@code scooter} form factor into {@code scooter_standing} and
   * {@code scooter_seated}, mapping the classic standing e-scooter to {@code scooter_standing}.
   * Since vehicles are now mapped from the v3 model, a client filtering on the (deprecated)
   * {@code SCOOTER} value would otherwise no longer match those vehicles.
   *
   * @param requested the requested form factors, or {@code null} for no filter
   * @return the requested form factors with {@link #SCOOTER_STANDING} added when {@link #SCOOTER}
   *     is present; the input is returned unchanged otherwise
   */
  public static List<FormFactor> expandFormFactorFilter(List<FormFactor> requested) {
    if (requested == null || !requested.contains(SCOOTER)) {
      return requested;
    }
    List<FormFactor> expanded = new ArrayList<>(requested);
    if (!expanded.contains(SCOOTER_STANDING)) {
      expanded.add(SCOOTER_STANDING);
    }
    return expanded;
  }
}
