/*
 *
 *
 *  * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 *  * the European Commission - subsequent versions of the EUPL (the "Licence");
 *  * You may not use this work except in compliance with the Licence.
 *  * You may obtain a copy of the Licence at:
 *  *
 *  *   https://joinup.ec.europa.eu/software/page/eupl
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the Licence is distributed on an "AS IS" basis,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the Licence for the specific language governing permissions and
 *  * limitations under the Licence.
 *
 */

package org.entur.lamassu.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.entur.lamassu.cache.GBFSV3FeedCache;
import org.entur.lamassu.config.feedprovider.FeedProviderConfig;
import org.entur.lamassu.model.provider.FeedProvider;
import org.mobilitydata.gbfs.v3_0.gbfs.GBFSFeed;
import org.mobilitydata.gbfs.v3_0.system_pricing_plans.GBFSPlan;
import org.mobilitydata.gbfs.v3_0.system_pricing_plans.GBFSSystemPricingPlans;
import org.mobilitydata.gbfs.v3_0.vehicle_types.GBFSVehicleType;
import org.mobilitydata.gbfs.v3_0.vehicle_types.GBFSVehicleTypes;
import org.springframework.stereotype.Service;

/**
 * Detects entity IDs that are claimed by more than one system within the same
 * codespace.
 *
 * <p>Entity IDs are namespaced by codespace, not by system (see
 * {@code IdMappers.mapId}), while all systems share one entity cache per entity
 * type. An operator that reuses the same {@code vehicle_type_id} or
 * {@code plan_id} across several of its systems therefore produces colliding
 * cache keys, and the system that updates last silently replaces the others.
 *
 * <p>This service is detection only. It never modifies cached data. It reads the
 * mapped feeds from the shared feed cache, so the IDs it compares are exactly the
 * keys that collide in the entity caches.
 *
 * <p>Duplicates <em>within</em> a single system's own feed file are deliberately
 * not reported here; those are already handled by
 * {@code EntityCollectors.toMapWithDuplicateWarning}.
 */
@Service
public class DuplicateIdService {

  public static final String ENTITY_VEHICLE_TYPE = "vehicleType";
  public static final String ENTITY_PRICING_PLAN = "pricingPlan";

  /**
   * An entity ID claimed by more than one system in the same codespace.
   */
  public record DuplicateId(String id, SortedSet<String> systemIds) {}

  /**
   * The duplicate IDs found for one codespace and entity type. Emitted for every
   * (codespace, entityType) pair in configuration, with an empty duplicates list
   * when there is nothing wrong, so that gauges can be driven back to zero.
   */
  public record DuplicateIdReport(
    String codespace,
    String entityType,
    List<DuplicateId> duplicates
  ) {}

  private record TrackedEntity(
    String name,
    GBFSFeed.Name feedName,
    Function<Object, Set<String>> extractIds
  ) {}

  private static final List<TrackedEntity> TRACKED_ENTITIES = List.of(
    new TrackedEntity(
      ENTITY_VEHICLE_TYPE,
      GBFSFeed.Name.VEHICLE_TYPES,
      DuplicateIdService::vehicleTypeIds
    ),
    new TrackedEntity(
      ENTITY_PRICING_PLAN,
      GBFSFeed.Name.SYSTEM_PRICING_PLANS,
      DuplicateIdService::pricingPlanIds
    )
  );

  private final FeedProviderConfig feedProviderConfig;
  private final GBFSV3FeedCache feedCache;

  public DuplicateIdService(
    FeedProviderConfig feedProviderConfig,
    GBFSV3FeedCache feedCache
  ) {
    this.feedProviderConfig = feedProviderConfig;
    this.feedCache = feedCache;
  }

  /**
   * Returns one report per (codespace, entity type), including codespaces with no
   * duplicates.
   */
  public List<DuplicateIdReport> detect() {
    Map<String, List<FeedProvider>> providersByCodespace = feedProviderConfig
      .getProviders()
      .stream()
      .collect(
        Collectors.groupingBy(
          FeedProvider::getCodespace,
          TreeMap::new,
          Collectors.toList()
        )
      );

    List<DuplicateIdReport> reports = new ArrayList<>();
    for (Map.Entry<String, List<FeedProvider>> entry : providersByCodespace.entrySet()) {
      for (TrackedEntity trackedEntity : TRACKED_ENTITIES) {
        reports.add(buildReport(entry.getKey(), entry.getValue(), trackedEntity));
      }
    }
    return reports;
  }

  private DuplicateIdReport buildReport(
    String codespace,
    List<FeedProvider> providers,
    TrackedEntity trackedEntity
  ) {
    Map<String, SortedSet<String>> claimsById = new TreeMap<>();
    for (FeedProvider provider : providers) {
      for (String id : idsFor(provider, trackedEntity)) {
        claimsById
          .computeIfAbsent(id, key -> new TreeSet<>())
          .add(provider.getSystemId());
      }
    }

    List<DuplicateId> duplicates = claimsById
      .entrySet()
      .stream()
      .filter(entry -> entry.getValue().size() > 1)
      .map(entry -> new DuplicateId(entry.getKey(), entry.getValue()))
      .toList();

    return new DuplicateIdReport(codespace, trackedEntity.name(), duplicates);
  }

  private Set<String> idsFor(FeedProvider provider, TrackedEntity trackedEntity) {
    Object feed = feedCache.find(trackedEntity.feedName(), provider);
    return feed == null ? Set.of() : trackedEntity.extractIds().apply(feed);
  }

  private static Set<String> vehicleTypeIds(Object feed) {
    if (
      !(feed instanceof GBFSVehicleTypes vehicleTypes) || vehicleTypes.getData() == null
    ) {
      return Set.of();
    }
    return nonBlankIds(
      vehicleTypes.getData().getVehicleTypes(),
      GBFSVehicleType::getVehicleTypeId
    );
  }

  private static Set<String> pricingPlanIds(Object feed) {
    if (
      !(feed instanceof GBFSSystemPricingPlans pricingPlans) ||
      pricingPlans.getData() == null
    ) {
      return Set.of();
    }
    return nonBlankIds(pricingPlans.getData().getPlans(), GBFSPlan::getPlanId);
  }

  private static <T> Set<String> nonBlankIds(List<T> items, Function<T, String> getId) {
    if (items == null) {
      return Set.of();
    }
    return items
      .stream()
      .map(getId)
      .filter(id -> id != null && !id.isBlank())
      .collect(Collectors.toSet());
  }
}
