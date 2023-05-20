package com.vhausler.property.stats.model.mapper;

import com.vhausler.property.stats.model.dto.LocationDTO;
import com.vhausler.property.stats.model.dto.ParameterDTO;
import com.vhausler.property.stats.model.dto.ScraperDTO;
import com.vhausler.property.stats.model.dto.ScraperResultDTO;
import com.vhausler.property.stats.model.entity.LocationEntity;
import com.vhausler.property.stats.model.entity.ParameterEntity;
import com.vhausler.property.stats.model.entity.ScraperEntity;
import com.vhausler.property.stats.model.entity.ScraperResultEntity;
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
    @Mapping(target = "scraperResultDTOS", source = "scraperResultEntities")
    ScraperDTO scraperEntityToScraperDTO(ScraperEntity scraperEntity);

    List<ScraperResultDTO> scraperResultEntitiesToScraperResultDTOS(List<ScraperResultEntity> scraperResultEntities);

    @Mapping(target = "scraperId", source = "scraperEntity.id")
    @Mapping(target = "parameterDTOS", source = "parameterEntities")
    ScraperResultDTO scraperResultEntityToScraperResultDTO(ScraperResultEntity scraperResultEntity);

    List<ParameterDTO> parameterEntitiesToParameterDTOS(List<ParameterEntity> parameterEntity);

    @Mapping(target = "scraperResultId", source = "scraperResult.id")
    ParameterDTO parameterEntityToParameterDTO(ParameterEntity parameterEntity);

    @Mapping(target = "locationEntity.id", source = "locationId")
    @Mapping(target = "scraperResultEntities", source = "scraperResultDTOS")
    ScraperEntity scraperDTOToScraperEntity(ScraperDTO scraperDTO);

    List<ScraperResultEntity> scraperResultDTOSToScraperResultEntities(List<ScraperResultDTO> scraperResultDTOS);

    @Mapping(target = "scraperEntity.id", source = "scraperId")
    @Mapping(target = "parameterEntities", source = "parameterDTOS")
    ScraperResultEntity scraperResultDTOToScraperResultEntity(ScraperResultDTO scraperResultDTO);

    List<ParameterEntity> parameterDTOSToParameterEntities(List<ParameterDTO> parameterDTOS);

    @Mapping(target = "scraperResult.id", source = "scraperResultId")
    ParameterEntity parameterDTOToParameterEntity(ParameterDTO parameterDTO);
}
