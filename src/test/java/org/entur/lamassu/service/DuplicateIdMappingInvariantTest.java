package org.entur.lamassu.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.entur.lamassu.mapper.entitymapper.PricingPlanMapper;
import org.entur.lamassu.mapper.entitymapper.TranslationMapper;
import org.entur.lamassu.mapper.entitymapper.VehicleTypeMapper;
import org.entur.lamassu.mapper.feedmapper.v3.V3SystemPricingPlansFeedMapper;
import org.entur.lamassu.mapper.feedmapper.v3.V3VehicleTypesFeedMapper;
import org.entur.lamassu.model.provider.FeedProvider;
import org.junit.jupiter.api.Test;
import org.mobilitydata.gbfs.v3_0.system_pricing_plans.GBFSPlan;
import org.mobilitydata.gbfs.v3_0.system_pricing_plans.GBFSSystemPricingPlans;
import org.mobilitydata.gbfs.v3_0.vehicle_types.GBFSVehicleType;
import org.mobilitydata.gbfs.v3_0.vehicle_types.GBFSVehicleTypes;

/**
 * DuplicateIdService (see {@link DuplicateIdService}) detects ids claimed by more
 * than one system by reading vehicle_type_id / plan_id straight out of the MAPPED
 * feeds in GBFSV3FeedCache. It never looks at EntityCache itself -- it just
 * assumes, without enforcing it, that the id string sitting in GBFSV3FeedCache is
 * exactly the id string that VehicleTypesUpdater / PricingPlansUpdater later use
 * as the EntityCache key.
 *
 * That assumption holds today because IdMappers.mapId(codespace, TYPE, rawId) is
 * applied exactly once, inside V3VehicleTypesFeedMapper / V3SystemPricingPlansFeedMapper,
 * before the mapped delivery is written to GBFSV3FeedCache, and the entity
 * mappers (VehicleTypeMapper#mapVehicleType, PricingPlanMapper#mapPricingPlan)
 * then copy that already-mapped id verbatim onto the entity.
 *
 * Nothing else pins this. If a future refactor moved the codespace-prefixing to
 * a different stage of the pipeline -- for example applying it only when
 * building EntityCache entries, and leaving the GBFSV3FeedCache payload with raw,
 * unprefixed ids -- DuplicateIdService would silently start comparing ids that no
 * longer correspond to real EntityCache keys, and would stop detecting real
 * collisions with no other test failing. This test exists to fail loudly the
 * moment that coupling breaks.
 */
class DuplicateIdMappingInvariantTest {

  private static final String CODESPACE = "YOS";

  private final TranslationMapper translationMapper = new TranslationMapper();
  private final V3VehicleTypesFeedMapper vehicleTypesFeedMapper =
    new V3VehicleTypesFeedMapper();
  private final VehicleTypeMapper vehicleTypeMapper = new VehicleTypeMapper(
    translationMapper
  );
  private final V3SystemPricingPlansFeedMapper pricingPlansFeedMapper =
    new V3SystemPricingPlansFeedMapper();
  private final PricingPlanMapper pricingPlanMapper = new PricingPlanMapper(
    translationMapper
  );

  @Test
  void vehicleTypeFeedCacheIdMatchesEntityCacheId() {
    FeedProvider feedProvider = provider();
    GBFSVehicleTypes rawFeed = new GBFSVehicleTypes()
      .withData(
        new org.mobilitydata.gbfs.v3_0.vehicle_types.GBFSData()
          .withVehicleTypes(List.of(rawVehicleType("scooter")))
      );

    // This is the mapping that populates GBFSV3FeedCache, i.e. what
    // DuplicateIdService reads.
    GBFSVehicleTypes mappedFeed = vehicleTypesFeedMapper.map(rawFeed, feedProvider);
    String feedCacheId = mappedFeed.getData().getVehicleTypes().get(0).getVehicleTypeId();

    // This is the mapping VehicleTypesUpdater uses to build the entity that
    // ends up in EntityCache, run on the SAME already-mapped feed object.
    String entityCacheId = vehicleTypeMapper
      .mapVehicleType(mappedFeed.getData().getVehicleTypes().get(0), "en")
      .getId();

    assertEquals(
      "YOS:VehicleType:scooter",
      feedCacheId,
      "the id read out of GBFSV3FeedCache should be codespace-prefixed, not the raw feed value"
    );
    assertEquals(
      feedCacheId,
      entityCacheId,
      "DuplicateIdService compares ids from GBFSV3FeedCache under the assumption " +
      "that they equal the EntityCache key produced by VehicleTypeMapper"
    );
  }

  @Test
  void pricingPlanFeedCacheIdMatchesEntityCacheId() {
    FeedProvider feedProvider = provider();
    GBFSSystemPricingPlans rawFeed = new GBFSSystemPricingPlans()
      .withData(
        new org.mobilitydata.gbfs.v3_0.system_pricing_plans.GBFSData()
          .withPlans(List.of(rawPlan("basic")))
      );

    // This is the mapping that populates GBFSV3FeedCache, i.e. what
    // DuplicateIdService reads.
    GBFSSystemPricingPlans mappedFeed = pricingPlansFeedMapper.map(rawFeed, feedProvider);
    String feedCacheId = mappedFeed.getData().getPlans().get(0).getPlanId();

    // This is the mapping PricingPlansUpdater uses to build the entity that
    // ends up in EntityCache, run on the SAME already-mapped feed object.
    String entityCacheId = pricingPlanMapper
      .mapPricingPlan(mappedFeed.getData().getPlans().get(0))
      .getId();

    assertEquals(
      "YOS:PricingPlan:basic",
      feedCacheId,
      "the id read out of GBFSV3FeedCache should be codespace-prefixed, not the raw feed value"
    );
    assertEquals(
      feedCacheId,
      entityCacheId,
      "DuplicateIdService compares ids from GBFSV3FeedCache under the assumption " +
      "that they equal the EntityCache key produced by PricingPlanMapper"
    );
  }

  private static FeedProvider provider() {
    FeedProvider provider = new FeedProvider();
    provider.setSystemId("yos_oslo");
    provider.setCodespace(CODESPACE);
    return provider;
  }

  private static GBFSVehicleType rawVehicleType(String rawId) {
    return new GBFSVehicleType()
      .withVehicleTypeId(rawId)
      .withFormFactor(GBFSVehicleType.FormFactor.SCOOTER_STANDING)
      .withPropulsionType(GBFSVehicleType.PropulsionType.ELECTRIC);
  }

  private static GBFSPlan rawPlan(String rawId) {
    return new GBFSPlan()
      .withPlanId(rawId)
      .withName(
        List.of(
          new org.mobilitydata.gbfs.v3_0.system_pricing_plans.GBFSName()
            .withLanguage("en")
            .withText("Basic")
        )
      )
      .withDescription(List.of())
      .withCurrency("NOK")
      .withPrice(0.0)
      .withIsTaxable(false);
  }
}
