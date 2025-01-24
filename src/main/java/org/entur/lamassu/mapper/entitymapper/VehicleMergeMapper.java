package org.entur.lamassu.mapper.entitymapper;

import org.entur.lamassu.model.entities.Vehicle;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
  componentModel = "spring",
  nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL,
  collectionMappingStrategy = CollectionMappingStrategy.TARGET_IMMUTABLE
)
public interface VehicleMergeMapper {
  /**
   * Updates an existing Vehicle with values from the source Vehicle.
   */
  void updateVehicle(@MappingTarget Vehicle target, Vehicle source);
}
