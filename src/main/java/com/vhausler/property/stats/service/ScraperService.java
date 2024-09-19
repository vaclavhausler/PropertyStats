package com.vhausler.property.stats.service;

import com.vhausler.property.stats.config.ConfigProperties;
import com.vhausler.property.stats.model.DriverWrapper;
import com.vhausler.property.stats.model.dto.ScraperDTO;
import com.vhausler.property.stats.model.dto.ScraperResultDTO;
import com.vhausler.property.stats.model.entity.ScraperEntity;
import com.vhausler.property.stats.model.entity.ScraperResultEntity;
import com.vhausler.property.stats.model.mapper.EntityMapper;
import com.vhausler.property.stats.model.repository.ParameterRepository;
import com.vhausler.property.stats.model.repository.ScraperRepository;
import com.vhausler.property.stats.model.repository.ScraperResultRepository;
import com.vhausler.property.stats.util.Util;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.vhausler.property.stats.util.Util.getCurrentTimestamp;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScraperService {

    private final EntityMapper entityMapper;
    private final DeletionService deletionService;
    private final ScraperRepository scraperRepository;
    private final ParameterRepository parameterRepository;
    private final ScraperResultRepository scraperResultRepository;
    private final ConfigProperties.PropertyStatsProperties propertyStatsProperties;

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void scrapeParams(DriverWrapper driverWrapper, ScraperResultDTO scraperResultDTO) {
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            Util.scrapePropertyParams(driverWrapper, scraperResultDTO);
            ScraperResultEntity scraperResultEntity = entityMapper.scraperResultDTOToScraperResultEntity(scraperResultDTO);
            if (!scraperResultEntity.isAvailable() && scraperResultEntity.getParameterEntities() == null) {
                scraperResultEntity.setParameterEntities(new ArrayList<>());
            }
            scraperResultRepository.save(scraperResultEntity);
            setParamsDone(scraperResultDTO);
            if (scraperResultEntity.isAvailable()) {
                if (scraperResultEntity.getParameterEntities() != null) {
                    log.trace("Property params saved: {}.", scraperResultEntity.getParameterEntities().size());
                } else {
                    log.trace("Property params saved: {}.", scraperResultEntity.getParameterEntities());
                }
            } else {
                log.trace("Offer no longer available: {}.", scraperResultDTO.getLink());
            }
        });
        try {
            future.get(240, TimeUnit.SECONDS);
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

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void setParamsDone(ScraperResultDTO scraperResultDTO) {
        scraperResultDTO.setParamsDone(getCurrentTimestamp());
        ScraperResultEntity scraperResultEntity = entityMapper.scraperResultDTOToScraperResultEntity(scraperResultDTO);
        scraperResultRepository.save(scraperResultEntity);
        log.trace("Setting scraper result params done for {}.", scraperResultEntity.getLink());
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void runPriceMaintenance() {
        Instant start = Instant.now();
        log.debug("Price maintenance START");
        AtomicInteger total = new AtomicInteger(0);
        AtomicInteger totalUpdated = new AtomicInteger(0);
        int batchSize = propertyStatsProperties.getMaintenanceBatchSize();
        Pageable pageable = Pageable.ofSize(batchSize);
        do {
            Page<ScraperResultEntity> page = scraperResultRepository.findAll(pageable);
            for (ScraperResultEntity scraperResultEntity : page.getContent()) {
                // most likely a mistake of adding extra '000'
                if (scraperResultEntity.getPrice() > 1000000000 && scraperResultEntity.getPrice() % 1000 == 0) {
                    scraperResultEntity.setPrice(scraperResultEntity.getPrice() / 1000);
                    totalUpdated.incrementAndGet();
                }
            }
            total.addAndGet(page.getContent().size());

            log.debug("Total processed: {}, total updated: {}.", total.get(), totalUpdated.get());

            pageable = page.nextPageable();
        } while (pageable.isPaged());
        log.debug("Price maintenance END in {} s.", Duration.between(start, Instant.now()).toSeconds());
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void runPricePerSquareMeterAndSquareMetersMaintenance() {
        Instant start = Instant.now();
        log.debug("Price per square meter and square meters maintenance START");
        AtomicInteger total = new AtomicInteger(0);
        AtomicInteger totalUpdated = new AtomicInteger(0);
        int batchSize = propertyStatsProperties.getMaintenanceBatchSize();
        Pageable pageable = Pageable.ofSize(batchSize);
        do {
            Page<ScraperResultEntity> page = scraperResultRepository.findAll(pageable);
            for (ScraperResultEntity scraperResultEntity : page.getContent()) {
                boolean updateRequired = false;
                String title = scraperResultEntity.getTitle();
                Integer expectedSquareMeters = Util.getSquareMeters(title);
                Integer actualSquareMeters = scraperResultEntity.getSquareMeters();
                if (expectedSquareMeters != null && expectedSquareMeters != 0 && !Objects.equals(actualSquareMeters, expectedSquareMeters)) {
                    scraperResultEntity.setSquareMeters(expectedSquareMeters);
                    updateRequired = true;
                }

                if (scraperResultEntity.getSquareMeters() != 0) {
                    Integer price = scraperResultEntity.getPrice();
                    Integer actualPricePerSquareMeter = scraperResultEntity.getPricePerSquareMeter();
                    Integer expectedPricePerSquareMeter = price / scraperResultEntity.getSquareMeters();
                    if (expectedPricePerSquareMeter != 0 && !Objects.equals(actualPricePerSquareMeter, expectedPricePerSquareMeter)) {
                        scraperResultEntity.setPricePerSquareMeter(expectedPricePerSquareMeter);
                        updateRequired = true;
                    }
                }
                if (updateRequired) {
                    scraperResultRepository.save(scraperResultEntity);
                    totalUpdated.incrementAndGet();
                }
            }
            total.addAndGet(page.getContent().size());

            log.debug("Total processed: {}, total updated: {}.", total.get(), totalUpdated.get());

            pageable = page.nextPageable();
        } while (pageable.isPaged());
        log.debug("Price per square meter and square meters maintenance END in {} s.", Duration.between(start, Instant.now()).toSeconds());
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void runScraperResultDuplicateMaintenance() {
        Instant start = Instant.now();
        log.debug("Scraper result duplicates maintenance START");
        AtomicInteger total = new AtomicInteger(0);

        List<ScraperResultEntity> deleteList = new ArrayList<>();
        Map<Pair<Long, String>, ScraperResultEntity> filterMap = new HashMap<>();
        List<ScraperResultEntity> all = scraperResultRepository.findAll();

        for (ScraperResultEntity scraperResultEntity : all) {
            long scraperId = scraperResultEntity.getScraperEntity().getId();
            Pair<Long, String> key = Pair.of(scraperId, scraperResultEntity.getLink());
            ScraperResultEntity found = filterMap.get(key);
            if (found == null) {
                filterMap.put(key, scraperResultEntity);
            } else {
                deleteList.add(scraperResultEntity);
            }
        }
        total.addAndGet(all.size());

        log.debug("Total processed: {}, total to delete: {}.", total.get(), deleteList.size());

        int deleteBatchSize = 50000;
        List<List<ScraperResultEntity>> partitions = ListUtils.partition(deleteList, deleteBatchSize);
        for (List<ScraperResultEntity> partition : partitions) {
            Instant start2 = Instant.now();
            deletionService.deleteScraperResult(partition);
            log.debug("Deleted {} scraper results in {} ms.", partition.size(), Duration.between(start2, Instant.now()).toMillis());
        }

        log.debug("Scraper result duplicates maintenance END in {} s.", Duration.between(start, Instant.now()).toSeconds());
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void runParamsDoneMaintenance() {
        Instant start = Instant.now();
        log.debug("Params done maintenance START");
        AtomicInteger scraperEntitiesCount = new AtomicInteger();
        AtomicInteger scraperResultEntitiesCount = new AtomicInteger();

        // params not done on scraper entities
        List<ScraperEntity> scraperEntities = scraperRepository.findAllByHeadersDoneIsNotNullAndParamsDoneIsNull();

        for (ScraperEntity scraperEntity : scraperEntities) {
            List<ScraperResultEntity> scraperResultEntities = scraperEntity.getScraperResultEntities();
            for (ScraperResultEntity scraperResultEntity : scraperResultEntities) {
                if (!scraperResultEntity.isAvailable() && scraperResultEntity.getParamsDone() == null) {
                    scraperResultEntity.setParamsDone(getCurrentTimestamp());
                    scraperResultRepository.save(scraperResultEntity);
                    scraperResultEntitiesCount.incrementAndGet();
                }
            }
            // count all that don't have params done, if there are none, mark the scraper entity as params done
            long count = scraperResultEntities.stream().filter(sre -> sre.getParamsDone() == null).count();
            if (count == 0) {
                scraperEntity.setParamsDone(getCurrentTimestamp());
                scraperRepository.save(scraperEntity);
                scraperEntitiesCount.incrementAndGet();
            }
        }

        // params not done on scraper result entities
        List<ScraperResultEntity> scraperResultEntities = scraperResultRepository.findAllByParamsDoneIsNull();
        for (ScraperResultEntity scraperResultEntity : scraperResultEntities) {
            if (!scraperResultEntity.isAvailable() && scraperResultEntity.getParamsDone() == null) {
                scraperResultEntity.setParamsDone(getCurrentTimestamp());
                scraperResultRepository.save(scraperResultEntity);
                scraperResultEntitiesCount.incrementAndGet();
            }
        }

        log.debug("Updated {} scraper entities and {} scraper result entities.", scraperEntitiesCount.get(), scraperResultEntitiesCount.get());

        log.debug("Params done maintenance END in {} s.", Duration.between(start, Instant.now()).toSeconds());
    }
}
