package org.entur.lamassu.mapper.entitymapper;

import org.entur.lamassu.model.entities.Station;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
  componentModel = "spring",
  nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL
)
public interface StationMergeMapper {
  /**
   * Updates an existing Station with non-null values from the source Station.
   */
  void updateStation(@MappingTarget Station target, Station source);
}
