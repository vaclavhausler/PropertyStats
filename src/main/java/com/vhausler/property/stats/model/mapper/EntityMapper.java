package com.vhausler.property.stats.model.mapper;

import com.vhausler.property.stats.model.dto.*;
import com.vhausler.property.stats.model.entity.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@SuppressWarnings("unused")
@Mapper(componentModel = "spring")
public interface EntityMapper {
    LocationDTO locationEntityToLocationDTO(LocationEntity locationEntity);

    LocationEntity locationDTOToLocationEntity(LocationDTO locationDTO);

    List<LocationDTO> locationEntitiesToLocationDTOS(List<LocationEntity> locationEntities);

    List<ScraperDTO> scraperEntitiesToScraperDTOS(List<ScraperEntity> scraperEntities);

    @Mapping(target = "locationId", source = "locationEntity.id")
    @Mapping(target = "scraperTypeDTO", source = "scraperTypeEntity")
    @Mapping(target = "scraperResultDTOS", source = "scraperResultEntities", ignore = true)
    ScraperDTO scraperEntityToScraperDTO(ScraperEntity scraperEntity);

    List<ScraperResultDTO> scraperResultEntitiesToScraperResultDTOS(List<ScraperResultEntity> scraperResultEntities);

    @Mapping(target = "scraperId", source = "scraperEntity.id")
    @Mapping(target = "parameterDTOS", source = "parameterEntities", ignore = true)
    ScraperResultDTO scraperResultEntityToScraperResultDTO(ScraperResultEntity scraperResultEntity);

    List<ParameterDTO> parameterEntitiesToParameterDTOS(List<ParameterEntity> parameterEntity);

    @Mapping(target = "scraperResultId", source = "scraperResultEntity.id")
    ParameterDTO parameterEntityToParameterDTO(ParameterEntity parameterEntity);

    @Mapping(target = "locationEntity.id", source = "locationId")
    @Mapping(target = "scraperTypeEntity", source = "scraperTypeDTO")
    @Mapping(target = "scraperResultEntities", source = "scraperResultDTOS")
    ScraperEntity scraperDTOToScraperEntity(ScraperDTO scraperDTO);

    List<ScraperResultEntity> scraperResultDTOSToScraperResultEntities(List<ScraperResultDTO> scraperResultDTOS);

    @Mapping(target = "scraperEntity.id", source = "scraperId")
    @Mapping(target = "parameterEntities", source = "parameterDTOS")
    @Mapping(target = "squareMeters", source = "squareMeters")
    ScraperResultEntity scraperResultDTOToScraperResultEntity(ScraperResultDTO scraperResultDTO);

    List<ParameterEntity> parameterDTOSToParameterEntities(List<ParameterDTO> parameterDTOS);

    @Mapping(target = "scraperResultEntity.id", source = "scraperResultId")
    ParameterEntity parameterDTOToParameterEntity(ParameterDTO parameterDTO);

    List<ScraperTypeDTO> scraperTypeEntitiesToScraperTypeDTOS(List<ScraperTypeEntity> scraperTypeEntities);
}
