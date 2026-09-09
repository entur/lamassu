package org.entur.lamassu.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.entur.lamassu.cache.GBFSV3FeedCache;
import org.entur.lamassu.config.feedprovider.FeedProviderConfig;
import org.entur.lamassu.model.provider.FeedProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mobilitydata.gbfs.v3_0.gbfs.GBFSFeed;
import org.mobilitydata.gbfs.v3_0.system_pricing_plans.GBFSPlan;
import org.mobilitydata.gbfs.v3_0.system_pricing_plans.GBFSSystemPricingPlans;
import org.mobilitydata.gbfs.v3_0.vehicle_types.GBFSVehicleType;
import org.mobilitydata.gbfs.v3_0.vehicle_types.GBFSVehicleTypes;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DuplicateIdServiceTest {

  @Mock
  private FeedProviderConfig feedProviderConfig;

  @Mock
  private GBFSV3FeedCache feedCache;

  private DuplicateIdService service;

  @BeforeEach
  void setUp() {
    service = new DuplicateIdService(feedProviderConfig, feedCache);
  }

  @Test
  void detectsVehicleTypeIdClaimedByTwoSystemsInSameCodespace() {
    FeedProvider oslo = provider("yos_oslo", "YOS");
    FeedProvider bergen = provider("yos_bergen", "YOS");
    when(feedProviderConfig.getProviders()).thenReturn(List.of(oslo, bergen));
    when(feedCache.find(GBFSFeed.Name.VEHICLE_TYPES, oslo))
      .thenReturn(vehicleTypes("YOS:VehicleType:scooter", "YOS:VehicleType:ebike"));
    when(feedCache.find(GBFSFeed.Name.VEHICLE_TYPES, bergen))
      .thenReturn(vehicleTypes("YOS:VehicleType:scooter"));

    DuplicateIdService.DuplicateIdReport report = reportFor(
      service.detect(),
      "YOS",
      DuplicateIdService.ENTITY_VEHICLE_TYPE
    );

    assertEquals(1, report.duplicates().size());
    assertEquals("YOS:VehicleType:scooter", report.duplicates().get(0).id());
    assertEquals(
      Set.of("yos_bergen", "yos_oslo"),
      report.duplicates().get(0).systemIds()
    );
  }

  @Test
  void doesNotReportSameIdInDifferentCodespaces() {
    FeedProvider yos = provider("yos_oslo", "YOS");
    FeedProvider tst = provider("tst_oslo", "TST");
    when(feedProviderConfig.getProviders()).thenReturn(List.of(yos, tst));
    when(feedCache.find(GBFSFeed.Name.VEHICLE_TYPES, yos))
      .thenReturn(vehicleTypes("YOS:VehicleType:scooter"));
    when(feedCache.find(GBFSFeed.Name.VEHICLE_TYPES, tst))
      .thenReturn(vehicleTypes("YOS:VehicleType:scooter"));

    List<DuplicateIdService.DuplicateIdReport> reports = service.detect();

    assertTrue(
      reportFor(reports, "YOS", DuplicateIdService.ENTITY_VEHICLE_TYPE)
        .duplicates()
        .isEmpty()
    );
    assertTrue(
      reportFor(reports, "TST", DuplicateIdService.ENTITY_VEHICLE_TYPE)
        .duplicates()
        .isEmpty()
    );
  }

  @Test
  void doesNotReportDuplicateWithinASingleSystemsOwnFeed() {
    FeedProvider oslo = provider("yos_oslo", "YOS");
    when(feedProviderConfig.getProviders()).thenReturn(List.of(oslo));
    when(feedCache.find(GBFSFeed.Name.VEHICLE_TYPES, oslo))
      .thenReturn(vehicleTypes("YOS:VehicleType:scooter", "YOS:VehicleType:scooter"));

    assertTrue(
      reportFor(service.detect(), "YOS", DuplicateIdService.ENTITY_VEHICLE_TYPE)
        .duplicates()
        .isEmpty()
    );
  }

  @Test
  void detectsPricingPlanIdClaimedByTwoSystemsInSameCodespace() {
    FeedProvider oslo = provider("yos_oslo", "YOS");
    FeedProvider bergen = provider("yos_bergen", "YOS");
    when(feedProviderConfig.getProviders()).thenReturn(List.of(oslo, bergen));
    when(feedCache.find(GBFSFeed.Name.SYSTEM_PRICING_PLANS, oslo))
      .thenReturn(pricingPlans("YOS:PricingPlan:basic"));
    when(feedCache.find(GBFSFeed.Name.SYSTEM_PRICING_PLANS, bergen))
      .thenReturn(pricingPlans("YOS:PricingPlan:basic", "YOS:PricingPlan:premium"));

    DuplicateIdService.DuplicateIdReport report = reportFor(
      service.detect(),
      "YOS",
      DuplicateIdService.ENTITY_PRICING_PLAN
    );

    assertEquals(1, report.duplicates().size());
    assertEquals("YOS:PricingPlan:basic", report.duplicates().get(0).id());
    assertEquals(
      Set.of("yos_bergen", "yos_oslo"),
      report.duplicates().get(0).systemIds()
    );
  }

  @Test
  void emitsReportForCleanCodespaceSoGaugeCanBeZeroed() {
    FeedProvider oslo = provider("yos_oslo", "YOS");
    when(feedProviderConfig.getProviders()).thenReturn(List.of(oslo));
    when(feedCache.find(GBFSFeed.Name.VEHICLE_TYPES, oslo))
      .thenReturn(vehicleTypes("YOS:VehicleType:scooter"));
    when(feedCache.find(GBFSFeed.Name.SYSTEM_PRICING_PLANS, oslo))
      .thenReturn(pricingPlans("YOS:PricingPlan:basic"));

    List<DuplicateIdService.DuplicateIdReport> reports = service.detect();

    assertEquals(2, reports.size());
    assertTrue(
      reportFor(reports, "YOS", DuplicateIdService.ENTITY_VEHICLE_TYPE)
        .duplicates()
        .isEmpty()
    );
    assertTrue(
      reportFor(reports, "YOS", DuplicateIdService.ENTITY_PRICING_PLAN)
        .duplicates()
        .isEmpty()
    );
  }

  static GBFSSystemPricingPlans pricingPlans(String... ids) {
    return new GBFSSystemPricingPlans()
      .withData(
        new org.mobilitydata.gbfs.v3_0.system_pricing_plans.GBFSData()
          .withPlans(Arrays.stream(ids).map(id -> new GBFSPlan().withPlanId(id)).toList())
      );
  }

  static FeedProvider provider(String systemId, String codespace) {
    FeedProvider provider = new FeedProvider();
    provider.setSystemId(systemId);
    provider.setCodespace(codespace);
    provider.setOperatorId(codespace + ":Operator:" + systemId);
    provider.setAggregate(true);
    provider.setEnabled(true);
    return provider;
  }

  static GBFSVehicleTypes vehicleTypes(String... ids) {
    return new GBFSVehicleTypes()
      .withData(
        new org.mobilitydata.gbfs.v3_0.vehicle_types.GBFSData()
          .withVehicleTypes(
            Arrays
              .stream(ids)
              .map(id -> new GBFSVehicleType().withVehicleTypeId(id))
              .toList()
          )
      );
  }

  static DuplicateIdService.DuplicateIdReport reportFor(
    List<DuplicateIdService.DuplicateIdReport> reports,
    String codespace,
    String entityType
  ) {
    return reports
      .stream()
      .filter(report ->
        report.codespace().equals(codespace) && report.entityType().equals(entityType)
      )
      .findFirst()
      .orElseThrow(() ->
        new AssertionError("No report for " + codespace + "/" + entityType)
      );
  }
}
