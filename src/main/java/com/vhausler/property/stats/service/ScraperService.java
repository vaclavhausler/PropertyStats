package com.vhausler.property.stats.service;

import com.vhausler.property.stats.model.DriverWrapper;
import com.vhausler.property.stats.model.dto.ScraperDTO;
import com.vhausler.property.stats.model.dto.ScraperResultDTO;
import com.vhausler.property.stats.model.entity.ScraperEntity;
import com.vhausler.property.stats.model.entity.ScraperResultEntity;
import com.vhausler.property.stats.model.mapper.EntityMapper;
import com.vhausler.property.stats.model.repository.ScraperRepository;
import com.vhausler.property.stats.model.repository.ScraperResultRepository;
import com.vhausler.property.stats.util.Util;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static com.vhausler.property.stats.util.Util.getCurrentTimestamp;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScraperService {

    private final EntityMapper entityMapper;
    private final ScraperRepository scraperRepository;
    private final ScraperResultRepository scraperResultRepository;

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void scrapeParams(DriverWrapper driverWrapper, ScraperResultDTO scraperResultDTO) {
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            Util.scrapePropertyParams(driverWrapper, scraperResultDTO);
            ScraperResultEntity scraperResultEntity = entityMapper.scraperResultDTOToScraperResultEntity(scraperResultDTO);
            scraperResultRepository.save(scraperResultEntity);
            log.trace("Property params saved: {}.", scraperResultEntity.getParameterEntities().size());
        });
        try {
            future.get(10, TimeUnit.SECONDS);
        } catch (Exception e) { // NOSONAR
            log.debug("Scrape params timeout. {}", scraperResultDTO.getLink());
            throw new IllegalStateException(e);
        }
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void setParamsDone(ScraperDTO scraperDTO) {
        scraperDTO.setParamsDone(getCurrentTimestamp());
        ScraperEntity scraperEntity = entityMapper.scraperDTOToScraperEntity(scraperDTO);
        scraperRepository.save(scraperEntity);
        log.trace("Setting scraper params done for {}.", scraperEntity.getLocationEntity().getId());
    }
}
