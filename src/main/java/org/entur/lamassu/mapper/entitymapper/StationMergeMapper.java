package org.entur.lamassu.mapper.entitymapper;

import org.entur.lamassu.model.entities.Station;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
  componentModel = "spring",
  nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL,
  collectionMappingStrategy = CollectionMappingStrategy.TARGET_IMMUTABLE
)
public interface StationMergeMapper {
  /**
   * Updates an existing Station with values from the source Station.
   */
  void updateStation(@MappingTarget Station target, Station source);
}
