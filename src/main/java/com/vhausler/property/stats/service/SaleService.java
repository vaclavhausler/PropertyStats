package com.vhausler.property.stats.service;

import com.vhausler.property.stats.config.ConfigProperties;
import com.vhausler.property.stats.model.DriverWrapper;
import com.vhausler.property.stats.model.dto.ScraperDTO;
import com.vhausler.property.stats.model.dto.ScraperResultDTO;
import com.vhausler.property.stats.model.entity.LocationEntity;
import com.vhausler.property.stats.model.entity.ScraperEntity;
import com.vhausler.property.stats.model.entity.ScraperResultEntity;
import com.vhausler.property.stats.model.mapper.EntityMapper;
import com.vhausler.property.stats.model.repository.LocationRepository;
import com.vhausler.property.stats.model.repository.ScraperRepository;
import com.vhausler.property.stats.model.repository.ScraperResultRepository;
import com.vhausler.property.stats.util.Util;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static com.vhausler.property.stats.util.Util.getCurrentTimestamp;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class SaleService {


    private final EntityMapper entityMapper;
    private final DriverService driverService;
    private final ScraperService scraperService;
    private final ScraperRepository scraperRepository;
    private final LocationRepository locationRepository;
    private final ScraperResultRepository scraperResultRepository;
    private final ConfigProperties.WebDriverProperties webDriverProperties;

    public void registerScrapers() {
        log.trace("Scheduler: registerScrapers. START.");
        List<LocationEntity> allLocationEntities = locationRepository.findAll();
        log.trace("Locations found: {}.", allLocationEntities.size());
        for (LocationEntity locationEntity : allLocationEntities) {
            // create new scraper entity
            ScraperEntity scraperEntity = new ScraperEntity();
            scraperEntity.setLocationEntity(locationEntity);
            log.debug("Creating scraper entity: {}.", scraperEntity);
            scraperEntity.setCreated(getCurrentTimestamp());
            scraperRepository.save(scraperEntity);
        }
        log.trace("Scheduler: registerScrapers. DONE.");
    }

    public void scrapeHeaders() {
        log.debug("Scheduler: scrapeHeaders. START.");
        Instant start = Instant.now();
        List<ScraperEntity> scraperEntities = scraperRepository.findAllByHeadersDoneIsNull();
        log.debug("Scraper headers fetched in {} ms.", Duration.between(start, Instant.now()).toMillis());
        start = Instant.now();
        List<ScraperDTO> scrapers = entityMapper.scraperEntitiesToScraperDTOS(scraperEntities);
        log.debug("Scraper headers mapped in {} ms.", Duration.between(start, Instant.now()).toMillis());
        if (!scrapers.isEmpty()) {
            // process anything unfinished
            for (ScraperDTO scraperDTO : scrapers) {
                if (scraperDTO.getHeadersDone() == null) {
                    log.debug("Scraping headers for: {}.", scraperDTO.getLocationId());
                    DriverWrapper driverWrapper = driverService.setupWebDriver(webDriverProperties.getHeadless());
                    try {
                        Optional<LocationEntity> locationEntityOptional = locationRepository.findById(scraperDTO.getLocationId());
                        if (locationEntityOptional.isPresent()) {
                            LocationEntity locationEntity = locationEntityOptional.get();
                            Util.scrapePropertyHeaders(driverWrapper, scraperDTO, locationEntity.getValue());
                            scraperDTO.setHeadersDone(getCurrentTimestamp());
                            ScraperEntity scraperEntity = entityMapper.scraperDTOToScraperEntity(scraperDTO);
                            scraperRepository.save(scraperEntity);
                            log.debug("Finished scraping headers for: {}.", scraperDTO.getLocationId());
                        } else {
                            log.warn("Location entity not found: {}.", scraperDTO.getLocationId());
                        }
                    } catch (Exception e) {
                        log.error("Exception scraping property params.", e);
                    } finally {
                        CompletableFuture.runAsync(driverWrapper::quit);
                    }
                }
            }
        }
        log.debug("Scheduler: scrapeHeaders. DONE.");
    }

    @Transactional
    public void scrapeParams() { // NOSONAR
        log.debug("Scheduler: scrapeParams. START.");
        Instant start = Instant.now();
        List<ScraperEntity> scraperEntities = scraperRepository.findAllByHeadersDoneIsNotNullAndParamsDoneIsNull();
        log.debug("{} Scraper params fetched in {} ms.", scraperEntities.size(), Duration.between(start, Instant.now()).toMillis());
        start = Instant.now();
        List<ScraperDTO> scrapers = entityMapper.scraperEntitiesToScraperDTOS(scraperEntities);
        log.debug("{} Scraper params mapped in {} ms.", scrapers.size(), Duration.between(start, Instant.now()).toMillis());
        if (!scrapers.isEmpty()) {
            // process anything unfinished
            for (ScraperDTO scraperDTO : scrapers) {
                if (scraperDTO.getParamsDone() == null) {
                    log.debug("Scraping params for scraper entity: {}.", scraperDTO);
                    DriverWrapper driverWrapper = driverService.setupWebDriver(webDriverProperties.getHeadless());
                    start = Instant.now();
                    List<ScraperResultEntity> scraperResultEntities = scraperResultRepository.findAllByScraperEntity_idAndParamsDoneIsNull(scraperDTO.getId());
                    log.debug("{} Scraper result entities fetched in {} ms.", scraperResultEntities.size(), Duration.between(start, Instant.now()).toMillis());
                    start = Instant.now();
                    List<ScraperResultDTO> scraperResultDTOS = entityMapper.scraperResultEntitiesToScraperResultDTOS(scraperResultEntities);
                    log.debug("{} Scraper result entities mapped in {} ms.", scraperResultDTOS.size(), Duration.between(start, Instant.now()).toMillis());
                    try {
                        int done = 0;
                        for (ScraperResultDTO scraperResultDTO : scraperResultDTOS) {
                            if (scraperResultDTO.getParameterDTOS() == null) {
                                scraperResultDTO.setAvailable(true); // liquibase/postgresql default doesn't work for some reason
                                scraperService.scrapeParams(driverWrapper, scraperResultDTO);
                                scraperService.setParamsDone(scraperResultDTO);
                            }
                            if (done++ % 20 == 0) {
                                log.debug("Finished scraping property params for {}/{} properties for {}.",
                                        done, scraperResultDTOS.size(), scraperDTO.getLocationId());
                            }
                        }
                        scraperService.setParamsDone(scraperDTO);
                    } catch (Exception e) {
                        log.error("Exception scraping property params. Restarting the webdriver.");
                    } finally {
                        CompletableFuture.runAsync(driverWrapper::quit);
                    }
                }
            }
        }
        log.debug("Scheduler: scrapeParams. DONE.");
    }
}
