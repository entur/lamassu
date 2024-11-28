package org.entur.lamassu.util;

import java.util.List;
import org.entur.gbfs.v2_3.vehicle_types.GBFSVehicleType;
import org.entur.lamassu.model.entities.FormFactor;
import org.entur.lamassu.model.provider.FeedProvider;
import org.junit.Assert;
import org.junit.Test;

public class OperatorFilterTest {

  @Test
  public void testFilterNoFormFactorGiven() {
    OperatorFilter filter = new OperatorFilter(null);
    Assert.assertTrue(filter.matches(carRentingMockProvider()));
  }

  @Test
  public void testFilterMatchingFormFactorGiven() {
    OperatorFilter filter = new OperatorFilter(List.of(FormFactor.CAR));
    Assert.assertTrue(filter.matches(carRentingMockProvider()));
  }

  @Test
  public void testFilterNotMatchingFormFactorGiven() {
    OperatorFilter filter = new OperatorFilter(List.of(FormFactor.BICYCLE));
    Assert.assertFalse(filter.matches(carRentingMockProvider()));
  }

  @Test
  public void testFilterMatchingOneOfMultipleFormFactorsGiven() {
    OperatorFilter filter = new OperatorFilter(
      List.of(FormFactor.BICYCLE, FormFactor.CAR)
    );
    Assert.assertTrue(filter.matches(carRentingMockProvider()));
  }

  private FeedProvider carRentingMockProvider() {
    var feedProvider = new FeedProvider();
    feedProvider.setSystemId("testsystem");
    feedProvider.setCodespace("TST");
    feedProvider.setLanguage("en");

    GBFSVehicleType carVehicleType = new GBFSVehicleType();
    carVehicleType.setFormFactor(GBFSVehicleType.FormFactor.CAR);
    feedProvider.setVehicleTypes(List.of(carVehicleType));
    return feedProvider;
  }
}
