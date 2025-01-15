package org.entur.lamassu.config.cache;

import org.entur.lamassu.cache.*;
import org.entur.lamassu.model.entities.*;
import org.entur.lamassu.model.entities.System;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class RequestScopedCacheConfig {

  @Bean
  @Primary
  public EntityReader<System> requestScopedSystemReader(
    @Qualifier("systemCacheImpl") EntityCache<System> systemCache,
    RequestScopedCache requestCache
  ) {
    return new RequestScopedEntityReader<>(systemCache, requestCache, "system");
  }

  @Bean
  @Primary
  public EntityReader<VehicleType> requestScopedVehicleTypeReader(
    @Qualifier("vehicleTypeCacheImpl") EntityCache<VehicleType> vehicleTypeCache,
    RequestScopedCache requestCache
  ) {
    return new RequestScopedEntityReader<>(vehicleTypeCache, requestCache, "vehicleType");
  }

  @Bean
  @Primary
  public EntityReader<PricingPlan> requestScopedPricingPlanReader(
    @Qualifier("pricingPlanCacheImpl") EntityCache<PricingPlan> pricingPlanCache,
    RequestScopedCache requestCache
  ) {
    return new RequestScopedEntityReader<>(pricingPlanCache, requestCache, "pricingPlan");
  }

  @Bean
  @Primary
  public EntityReader<Region> requestScopedRegionReader(
    @Qualifier("regionCacheImpl") EntityCache<Region> regionCache,
    RequestScopedCache requestCache
  ) {
    return new RequestScopedEntityReader<>(regionCache, requestCache, "region");
  }
}
