package com.vhausler.property.stats.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vhausler.property.stats.config.ConfigProperties;
import com.vhausler.property.stats.model.DriverWrapper;
import com.vhausler.property.stats.model.dto.*;
import com.vhausler.property.stats.model.entity.LocationEntity;
import com.vhausler.property.stats.model.entity.ScraperEntity;
import com.vhausler.property.stats.model.entity.ScraperResultEntity;
import com.vhausler.property.stats.model.entity.ScraperTypeEntity;
import com.vhausler.property.stats.model.mapper.EntityMapper;
import com.vhausler.property.stats.model.migration.MigrationPackage;
import com.vhausler.property.stats.model.repository.*;
import com.vhausler.property.stats.util.Util;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    // mapper
    private final EntityMapper entityMapper;

    // service
    private final DriverService driverService;
    private final ScraperService scraperService;

    // repository
    private final ScraperRepository scraperRepository;
    private final LocationRepository locationRepository;
    private final ParameterRepository parameterRepository;
    private final ScraperTypeRepository scraperTypeRepository;
    private final ScraperResultRepository scraperResultRepository;

    // config props
    private final ConfigProperties.WebDriverProperties webDriverProperties;
    private final ConfigProperties.PropertyStatsProperties propertyStatsProperties;

    // parameter cache
    private final AtomicInteger scraperResultCacheCounter = new AtomicInteger(0);
    private final Map<String, ScraperResultDTO> scraperResultCache = Collections.synchronizedMap(new HashMap<>());

    public void registerScrapers(ScraperRegistrationRequest scraperRegistrationRequest) {
        for (String scraperTypeId : scraperRegistrationRequest.getScraperTypeIds()) {
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
    }

    public void scrapeHeaders() { // NOSONAR
        log.trace("Started scraping offers.");
        Instant start = Instant.now();
        List<ScraperEntity> scraperEntities = scraperRepository.findAllByHeadersDoneIsNull();
        if (scraperEntities.isEmpty()) {
            log.trace("Nothing to process.");
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
                        DriverWrapper driverWrapper = driverService.setupWebDriverSReality(webDriverProperties.getHeadless(), scraperDTO.getScraperTypeDTO().getSearchValue());
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
        log.trace("Started scraping parameters for scraper results.");
        Instant start = Instant.now();

        // make sure that headers are done, return if not
        List<ScraperEntity> scraperEntities = scraperRepository.findAllByHeadersDoneIsNull();
        if (!scraperEntities.isEmpty()) {
            log.trace("Headers not done, skipping.");
            return;
        }

        scraperEntities = scraperRepository.findAllByHeadersDoneIsNotNullAndParamsDoneIsNull();
        if (scraperEntities.isEmpty()) {
            log.trace("Nothing to process.");
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

            // load the cache
            loadScraperResultDTOCache();
//            pool.execute(this::loadScraperResultDTOCache);

            // process anything unfinished
            for (ScraperDTO scraperDTO : scraperDTOS) {
                if (scraperDTO.getParamsDone() == null) {
                    Runnable r = () -> {
                        DriverWrapper driverWrapper = driverService.setupWebDriverSReality(webDriverProperties.getHeadless(), scraperDTO.getScraperTypeDTO().getSearchValue());
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
                                        if (scraperResultDTO.getParameterDTOS() == null) {
                                            scraperResultDTO.setParameterDTOS(entityMapper.parameterEntitiesToParameterDTOS(parameterRepository.findAllByScraperResultEntity_id(scraperResultDTO.getId())));
                                        }
                                        for (ParameterDTO parameterDTO : scraperResultDTO.getParameterDTOS()) {
                                            // set the scraper result id to the parameter
                                            parameterDTO.setScraperResultId(scraperResultDTO.getId());
                                        }

                                        scraperResultCacheCounter.incrementAndGet();
                                        if (scraperResultCacheCounter.get() % 10 == 0) {
                                            log.debug("Cache used: {} times.", scraperResultCacheCounter.get());
                                        }

                                        scraperService.setParamsDone(scraperResultDTO);
                                    } else {
                                        scraperResultDTO.setAvailable(true); // liquibase/postgresql default doesn't work for some reason
                                        scraperService.scrapeParams(driverWrapper, scraperResultDTO);

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
                            log.error("Exception scraping property params: {}. Restarting the webdriver.", e.getMessage(), e);
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

    public void loadScraperResultDTOCache() {
        log.debug("Loading scraper result cache.");
        Instant start = Instant.now();
        Page<ScraperResultEntity> linksPage = scraperResultRepository.findDistinctLinksLast6Months(Pageable.ofSize(propertyStatsProperties.getBatchSize()));
        linksPage.get()
                .filter(ScraperResultEntity::isAvailable)
                .forEach(e -> scraperResultCache.put(e.getLink(), entityMapper.scraperResultEntityToScraperResultDTO(e)));
        while (linksPage.hasNext()) {
            linksPage = scraperResultRepository.findDistinctLinksLast6Months(linksPage.nextPageable());
            linksPage.get()
                    .filter(ScraperResultEntity::isAvailable)
                    .forEach(e -> scraperResultCache.put(e.getLink(), entityMapper.scraperResultEntityToScraperResultDTO(e)));
            log.debug("Scraper result cache contains: {} items.", scraperResultCache.size());
        }
        log.debug("Finished loading scraper result cache in {} ms.", Duration.between(start, Instant.now()).toMillis());
    }

    public void runMaintenance(MaintenanceRequest maintenanceRequest) {
        Instant start = Instant.now();
        log.debug("Running maintenance: {}.", maintenanceRequest.getMaintenanceEnumList());
        List<MaintenanceEnum> maintenanceEnumList = maintenanceRequest.getMaintenanceEnumList();
        if (maintenanceEnumList.contains(MaintenanceEnum.PRICE_MAINTENANCE)) {
            scraperService.runPriceMaintenance();
        }
        if (maintenanceEnumList.contains(MaintenanceEnum.PRICE_PER_SQUARE_METER_AND_SQUARE_METERS_MAINTENANCE)) {
            scraperService.runPricePerSquareMeterAndSquareMetersMaintenance();
        }
        if (maintenanceEnumList.contains(MaintenanceEnum.SCRAPER_RESULT_DUPLICATES_MAINTENANCE)) {
            scraperService.runScraperResultDuplicateMaintenance();
        }
        if (maintenanceEnumList.contains(MaintenanceEnum.PARAMS_DONE)) {
            scraperService.runParamsDoneMaintenance();
        }
        log.debug("Maintenance done in {} s.", Duration.between(start, Instant.now()).getSeconds());
    }

    public List<ScraperEntity> fetchDataForMigration() {
        log.debug("Fetching data for migration.");
        Instant start = Instant.now();
        List<ScraperEntity> scraperEntities = scraperRepository.fetchDataForMigration();
        //noinspection ReplaceInefficientStreamCount,ResultOfMethodCallIgnored
        scraperEntities.forEach(se -> se.getScraperResultEntities().forEach(sre -> sre.getParameterEntities().stream().count())); // force fetch everything
        log.debug("Data fetched in {}.", Duration.between(start, Instant.now()).toString());

        ObjectMapper objectMapper = new ObjectMapper();
        MigrationPackage migrationPackage = new MigrationPackage();
        migrationPackage.setScraperEntities(scraperEntities);

        try {
            MigrationPackage newMigrationPackage = objectMapper.readValue(objectMapper.writeValueAsString(migrationPackage), MigrationPackage.class);
            return newMigrationPackage.getScraperEntities();
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    public void migrateData(List<ScraperEntity> allScraperEntities) {
        log.debug("Migrating {} scraper entities.", allScraperEntities.size());
        Instant start = Instant.now();

        AtomicInteger scraperEntityCount = new AtomicInteger();
        AtomicInteger scraperEntityResultCount = new AtomicInteger();

        for (ScraperEntity scraperEntity : allScraperEntities) {
            scraperEntity.setId(null);
            ScraperEntity newScraperEntity = scraperRepository.save(scraperEntity);

            log.debug("Scraper entities migrated: {}.", scraperEntityCount.incrementAndGet());

            List<ScraperResultEntity> scraperResultEntities = scraperEntity.getScraperResultEntities();
            for (ScraperResultEntity scraperResultEntity : scraperResultEntities) {
                scraperResultEntity.setId(null);
                scraperResultEntity.setScraperEntity(newScraperEntity);
                scraperResultEntity.getParameterEntities().forEach(p -> p.setId(null));

                scraperResultRepository.save(scraperResultEntity);
            }
            int sreCount = scraperEntityResultCount.addAndGet(scraperResultEntities.size());
            if (sreCount > 0) {
                log.debug("Scraper entity results migrated: {}.", sreCount);
            }
        }
        log.debug("Migrating {} scraper entities done in {}.", allScraperEntities.size(), Duration.between(start, Instant.now()).toString());
    }
}
