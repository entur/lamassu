package org.entur.lamassu.mapper.entitymapper;

import org.entur.lamassu.model.entities.Vehicle;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
  componentModel = "spring",
  nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface VehicleMergeMapper {
  /**
   * Updates an existing Vehicle with non-null values from the source Vehicle.
   */
  void updateVehicle(@MappingTarget Vehicle target, Vehicle source);
}
