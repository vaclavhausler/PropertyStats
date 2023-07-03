package com.vhausler.property.stats.service;

import com.vhausler.property.stats.config.ConfigProperties;
import com.vhausler.property.stats.model.DriverWrapper;
import com.vhausler.property.stats.model.dto.ParameterDTO;
import com.vhausler.property.stats.model.dto.ScraperDTO;
import com.vhausler.property.stats.model.dto.ScraperResultDTO;
import com.vhausler.property.stats.model.dto.ScraperTypeDTO;
import com.vhausler.property.stats.model.entity.LocationEntity;
import com.vhausler.property.stats.model.entity.ScraperEntity;
import com.vhausler.property.stats.model.entity.ScraperResultEntity;
import com.vhausler.property.stats.model.entity.ScraperTypeEntity;
import com.vhausler.property.stats.model.mapper.EntityMapper;
import com.vhausler.property.stats.model.repository.LocationRepository;
import com.vhausler.property.stats.model.repository.ScraperRepository;
import com.vhausler.property.stats.model.repository.ScraperResultRepository;
import com.vhausler.property.stats.model.repository.ScraperTypeRepository;
import com.vhausler.property.stats.util.Util;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.vhausler.property.stats.util.Util.getCurrentTimestamp;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class SRealityService {


    private final EntityMapper entityMapper;
    private final DriverService driverService;
    private final ScraperService scraperService;
    private final ScraperRepository scraperRepository;
    private final LocationRepository locationRepository;
    private final ScraperTypeRepository scraperTypeRepository;
    private final ScraperResultRepository scraperResultRepository;
    private final ConfigProperties.WebDriverProperties webDriverProperties;
    private final ConfigProperties.PropertyStatsProperties propertyStatsProperties;

    // parameter cache
    private final AtomicInteger scraperResultCacheCounter = new AtomicInteger(0);

    /*
        Find all scraper results for the current day and add them to the scraper result cache. Maybe in a separate thread?
     */
    private final Map<String, ScraperResultDTO> scraperResultCache = Collections.synchronizedMap(new HashMap<>()); // TODO implement slow loading cache? Paging?

    public void registerScrapers(String scraperTypeId) {
        log.trace("Started creating scraper headers.");

        Optional<ScraperTypeEntity> scraperTypeOpt = scraperTypeRepository.findById(scraperTypeId);
        if (scraperTypeOpt.isEmpty()) {
            throw new IllegalStateException(String.format("Scraper type for id: %s not found.", scraperTypeId));
        }

        ScraperTypeEntity scraperTypeEntity = scraperTypeOpt.get();

        List<LocationEntity> allLocationEntities = locationRepository.findAll();
        log.trace("Locations found: {}.", allLocationEntities.size());
        for (LocationEntity locationEntity : allLocationEntities) {
            // create new scraper entity
            ScraperEntity scraperEntity = new ScraperEntity();
            scraperEntity.setLocationEntity(locationEntity);
            log.debug("Creating scraper entity: {}.", scraperEntity);
            scraperEntity.setCreated(getCurrentTimestamp());
            scraperEntity.setScraperTypeEntity(scraperTypeEntity);
            scraperRepository.save(scraperEntity);
        }
        log.trace("Finished creating scraper headers.");
    }

    public void scrapeHeaders() { // NOSONAR
        log.debug("Started scraping offers.");
        Instant start = Instant.now();
        List<ScraperEntity> scraperEntities = scraperRepository.findAllByHeadersDoneIsNull();
        if (scraperEntities.isEmpty()) {
            log.debug("Nothing to process.");
            return;
        }
        log.debug("Scraper headers fetched in {} ms.", Duration.between(start, Instant.now()).toMillis());
        start = Instant.now();
        List<ScraperDTO> scrapers = entityMapper.scraperEntitiesToScraperDTOS(scraperEntities);

        log.debug("Scraper headers mapped in {} ms.", Duration.between(start, Instant.now()).toMillis());
        if (!scrapers.isEmpty()) {

            // thread-safe collection
            Collection<ScraperDTO> scraperDTOS = Collections.synchronizedCollection(scrapers);

            // executor service
            ExecutorService pool = Executors.newFixedThreadPool(propertyStatsProperties.getHeadersThreadCount());

            // process anything unfinished
            for (ScraperDTO scraperDTO : scraperDTOS) {
                if (scraperDTO.getHeadersDone() == null) {
                    Runnable r = () -> {
                        DriverWrapper driverWrapper = driverService.setupWebDriver(webDriverProperties.getHeadless(), scraperDTO.getScraperTypeDTO().getSearchValue());
                        log.debug("{}: Scraping headers for: {}.", driverWrapper.getName(), scraperDTO.getLocationId());
                        try {
                            Optional<LocationEntity> locationEntityOptional = locationRepository.findById(scraperDTO.getLocationId());
                            if (locationEntityOptional.isPresent()) {
                                LocationEntity locationEntity = locationEntityOptional.get();
                                Util.scrapePropertyHeaders(driverWrapper, scraperDTO, locationEntity.getValue());
                                scraperDTO.setHeadersDone(getCurrentTimestamp());
                                ScraperEntity scraperEntity = entityMapper.scraperDTOToScraperEntity(scraperDTO);
                                scraperRepository.save(scraperEntity);
                                scraperResultRepository.saveAll(scraperEntity.getScraperResultEntities());
                                log.debug("{}: Finished scraping headers for: {}.", driverWrapper.getName(), scraperDTO.getLocationId());
                            } else {
                                log.warn("{}: Location entity not found: {}.", driverWrapper.getName(), scraperDTO.getLocationId());
                            }
                        } catch (Exception e) {
                            log.error("{}: Exception scraping property params.", driverWrapper.getName(), e);
                        } finally {
                            CompletableFuture.runAsync(driverWrapper::quit);
                        }
                    };
                    pool.execute(r);
                }
            }
            try {
                pool.shutdown();
                boolean awaitedTermination = pool.awaitTermination(24, TimeUnit.HOURS);
                if (awaitedTermination) {
                    log.debug("Scheduler: scrapeHeaders. DONE.");
                } else {
                    log.debug("Scheduler: scrapeHeaders. TIMEOUT.");
                }
            } catch (InterruptedException e) { // NOSONAR
                throw new IllegalStateException(e);
            }
        }
    }

    @Transactional
    public void scrapeParams() { // NOSONAR
        log.debug("Started scraping parameters for scraper results.");
        Instant start = Instant.now();
        // make sure that headers are done, return if not
        List<ScraperEntity> scraperEntities = scraperRepository.findAllByHeadersDoneIsNull();
        if (!scraperEntities.isEmpty()) {
            log.debug("Headers not done, skipping.");
            return;
        }

        scraperEntities = scraperRepository.findAllByHeadersDoneIsNotNullAndParamsDoneIsNull();
        if (scraperEntities.isEmpty()) {
            log.debug("Nothing to process.");
            return;
        }
        log.debug("{} scraper results to process fetched in {} ms.", scraperEntities.size(), Duration.between(start, Instant.now()).toMillis());
        start = Instant.now();
        List<ScraperDTO> scrapers = entityMapper.scraperEntitiesToScraperDTOS(scraperEntities);

        log.debug("{} scraper results to process mapped in {} ms.", scrapers.size(), Duration.between(start, Instant.now()).toMillis());
        if (!scrapers.isEmpty()) {

            // thread-safe collection
            Collection<ScraperDTO> scraperDTOS = Collections.synchronizedCollection(scrapers);

            // executor service
            ExecutorService pool = Executors.newFixedThreadPool(propertyStatsProperties.getParamsThreadCount());

            // process anything unfinished
            for (ScraperDTO scraperDTO : scraperDTOS) {
                if (scraperDTO.getParamsDone() == null) {
                    Runnable r = () -> {
                        DriverWrapper driverWrapper = driverService.setupWebDriver(webDriverProperties.getHeadless(), scraperDTO.getScraperTypeDTO().getSearchValue());
                        log.debug("{}: Scraping params for scraper entity: {}.", driverWrapper.getName(), scraperDTO);
                        Instant start1 = Instant.now();
                        List<ScraperResultEntity> scraperResultEntities = scraperResultRepository.findAllByScraperEntity_idAndParamsDoneIsNull(scraperDTO.getId());
                        log.debug("{}: {} Scraper result entities fetched in {} ms.", driverWrapper.getName(), scraperResultEntities.size(), Duration.between(start1, Instant.now()).toMillis());
                        start1 = Instant.now();
                        List<ScraperResultDTO> scraperResultDTOS = entityMapper.scraperResultEntitiesToScraperResultDTOS(scraperResultEntities);
                        log.debug("{}: {} Scraper result entities mapped in {} ms.", driverWrapper.getName(), scraperResultDTOS.size(), Duration.between(start1, Instant.now()).toMillis());
                        try {
                            int done = 0;
                            for (ScraperResultDTO scraperResultDTO : scraperResultDTOS) {
                                if (scraperResultDTO.getParameterDTOS() == null) {
                                    // cache
                                    String link = scraperResultDTO.getLink();
                                    if (scraperResultCache.containsKey(link)) {

                                        // copy availability and parameters
                                        ScraperResultDTO cachedScraperResult = scraperResultCache.get(link);
                                        scraperResultDTO.setAvailable(cachedScraperResult.isAvailable());
                                        scraperResultDTO.setParameterDTOS(cachedScraperResult.getParameterDTOS());
                                        for (ParameterDTO parameterDTO : scraperResultDTO.getParameterDTOS()) {
                                            // set the scraper result id to the parameter
                                            parameterDTO.setScraperResultId(scraperResultDTO.getId());
                                        }

                                        scraperResultCacheCounter.incrementAndGet();
                                        log.debug("Cache used: {} times.", scraperResultCacheCounter.get());

                                        scraperService.setParamsDone(scraperResultDTO);
                                    } else {
                                        scraperResultDTO.setAvailable(true); // liquibase/postgresql default doesn't work for some reason
                                        scraperService.scrapeParams(driverWrapper, scraperResultDTO);
                                        scraperService.setParamsDone(scraperResultDTO);

                                        scraperResultCache.put(scraperResultDTO.getLink(), scraperResultDTO);
                                    }
                                }
                                if (done++ % 20 == 0) {
                                    log.debug("{}: Finished scraping property params for {}/{} properties for {}.",
                                            driverWrapper.getName(), done, scraperResultDTOS.size(), scraperDTO.getLocationId());
                                }
                            }
                            scraperService.setParamsDone(scraperDTO);
                        } catch (Exception e) {
                            log.error("Exception scraping property params. Restarting the webdriver.");
                        } finally {
                            CompletableFuture.runAsync(driverWrapper::quit);
                        }
                    };
                    pool.execute(r);
                }
            }
            try {
                pool.shutdown();
                boolean awaitedTermination = pool.awaitTermination(24, TimeUnit.HOURS);
                if (awaitedTermination) {
                    log.debug("Finished scraping parameters for scraper results.");
                } else {
                    log.debug("Time out scraping parameters for scraper results.");
                }
            } catch (InterruptedException e) { // NOSONAR
                throw new IllegalStateException(e);
            }
        }
    }

    public List<ScraperTypeDTO> getScraperTypes() {
        return entityMapper.scraperTypeEntitiesToScraperTypeDTOS((List<ScraperTypeEntity>) scraperTypeRepository.findAll());
    }
}
