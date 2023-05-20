package com.vhausler.property.stats.service;

import com.vhausler.property.stats.model.DriverWrapper;
import com.vhausler.property.stats.model.dto.ScraperResultDTO;
import com.vhausler.property.stats.model.entity.ScraperResultEntity;
import com.vhausler.property.stats.model.mapper.EntityMapper;
import com.vhausler.property.stats.model.repository.ScraperResultRepository;
import com.vhausler.property.stats.util.Util;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScraperParamService {

    private final EntityMapper entityMapper;
    private final ScraperResultRepository scraperResultRepository;

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void scrapeParams(DriverWrapper driverWrapper, ScraperResultDTO scraperResultDTO) {
        Util.scrapePropertyParams(driverWrapper, scraperResultDTO);
        ScraperResultEntity scraperResultEntity = entityMapper.scraperResultDTOToScraperResultEntity(scraperResultDTO);
        scraperResultRepository.save(scraperResultEntity);
        log.trace("Property params saved: {}.", scraperResultEntity.getParameterEntities().size());
    }
}
