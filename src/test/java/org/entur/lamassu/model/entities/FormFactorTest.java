package org.entur.lamassu.model.entities;

import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class FormFactorTest {

  @Test
  public void scooterFilterAlsoMatchesScooterStanding() {
    // GBFS v3 split the v2 "scooter" form factor into scooter_standing/scooter_seated.
    // Vehicles from v2 feeds are now mapped to SCOOTER_STANDING, so a filter on the
    // deprecated SCOOTER value must still include them.
    List<FormFactor> expanded = FormFactor.expandFormFactorFilter(
      List.of(FormFactor.SCOOTER)
    );
    Assert.assertTrue(expanded.contains(FormFactor.SCOOTER));
    Assert.assertTrue(expanded.contains(FormFactor.SCOOTER_STANDING));
  }

  @Test
  public void scooterFilterDoesNotMatchScooterSeated() {
    List<FormFactor> expanded = FormFactor.expandFormFactorFilter(
      List.of(FormFactor.SCOOTER)
    );
    Assert.assertFalse(expanded.contains(FormFactor.SCOOTER_SEATED));
  }

  @Test
  public void nonScooterFiltersAreUnchanged() {
    List<FormFactor> requested = List.of(FormFactor.BICYCLE, FormFactor.MOPED);
    Assert.assertEquals(requested, FormFactor.expandFormFactorFilter(requested));
  }

  @Test
  public void scooterStandingFilterIsNotDuplicated() {
    List<FormFactor> expanded = FormFactor.expandFormFactorFilter(
      List.of(FormFactor.SCOOTER, FormFactor.SCOOTER_STANDING)
    );
    Assert.assertEquals(
      1,
      expanded.stream().filter(f -> f == FormFactor.SCOOTER_STANDING).count()
    );
  }

  @Test
  public void nullFilterReturnsNull() {
    Assert.assertNull(FormFactor.expandFormFactorFilter(null));
  }
}
