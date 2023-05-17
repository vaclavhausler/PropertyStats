package com.vhausler.property.stats.model.mapper;

import com.vhausler.property.stats.model.dto.LocationDTO;
import com.vhausler.property.stats.model.entity.LocationEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EntityMapper {
    LocationDTO locationEntityToLocationDTO(LocationEntity locationEntity);

    LocationEntity locationDTOToLocationEntity(LocationDTO locationDTO);

    List<LocationDTO> locationEntitiesToLocationDTOs(List<LocationEntity> locationEntities);
}
