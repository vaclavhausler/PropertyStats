package com.vhausler.property.stats.service;

import com.vhausler.property.stats.config.ConfigProperties;
import com.vhausler.property.stats.model.DriverWrapper;
import com.vhausler.property.stats.model.entity.LocationEntity;
import com.vhausler.property.stats.model.entity.ScraperEntity;
import com.vhausler.property.stats.model.entity.ScraperResultEntity;
import com.vhausler.property.stats.model.repository.LocationRepository;
import com.vhausler.property.stats.model.repository.ScraperRepository;
import com.vhausler.property.stats.util.Util;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.vhausler.property.stats.util.Util.getCurrentTimestamp;

@Slf4j
@Service
@RequiredArgsConstructor
public class SaleService {

    private final DriverService driverService;
    private final ScraperRepository scraperRepository;
    private final LocationRepository locationRepository;
    private final ConfigProperties.WebDriverProperties webDriverProperties;

    public List<LocationEntity> getAllLocationEntities() {
        return (List<LocationEntity>) locationRepository.findAll();
    }

    public void registerScrapers() {
        List<LocationEntity> allLocationEntities = getAllLocationEntities();
        for (LocationEntity locationEntity : allLocationEntities) {
            List<ScraperEntity> allByLocationEntity = scraperRepository.findAllByLocationEntity(locationEntity);
            if (allByLocationEntity.isEmpty()) {
                // create new scraper entity
                ScraperEntity scraperEntity = new ScraperEntity();
                scraperEntity.setLocationEntity(locationEntity);
                log.debug("Creating scraper entity: {}.", scraperEntity);
                scraperEntity.setCreated(getCurrentTimestamp());
                scraperRepository.save(scraperEntity);
            }
        }
    }

    public void scrapeHeaders() {
        List<LocationEntity> allLocationEntities = getAllLocationEntities();
        for (LocationEntity locationEntity : allLocationEntities) {
            List<ScraperEntity> allByLocationEntity = scraperRepository.findAllByLocationEntity(locationEntity);
            if (!allByLocationEntity.isEmpty()) {
                // process anything unfinished
                for (ScraperEntity scraperEntity : allByLocationEntity) {
                    if (scraperEntity.getHeadersDone() == null) {
                        log.debug("Scraping headers for scraper entity: {}.", scraperEntity);
                        DriverWrapper driverWrapper = driverService.setupWebDriver(webDriverProperties.getHeadless());
                        try {
                            Util.scrapePropertyHeaders(driverWrapper, scraperEntity);
                            scraperEntity.setHeadersDone(getCurrentTimestamp());
                            scraperRepository.save(scraperEntity);
                        } catch (Exception e) {
                            log.error("Exception scraping property params.", e);
                        } finally {
                            driverWrapper.quit();
                        }
                    }
                }
            }
        }
    }

    public void scrapeParams() {
        List<LocationEntity> allLocationEntities = getAllLocationEntities();
        for (LocationEntity locationEntity : allLocationEntities) {
            List<ScraperEntity> allByLocationEntity = scraperRepository.findAllByLocationEntity(locationEntity);
            if (!allByLocationEntity.isEmpty()) {
                // process anything unfinished
                for (ScraperEntity scraperEntity : allByLocationEntity) {
                    if (scraperEntity.getParamsDone() == null) {
                        log.debug("Scraping params for scraper entity: {}.", scraperEntity);
                        DriverWrapper driverWrapper = driverService.setupWebDriver(webDriverProperties.getHeadless());
                        try {
                            for (ScraperResultEntity scraperResultEntity : scraperEntity.getScraperResultEntities()) {
                                Util.scrapePropertyParams(driverWrapper, scraperResultEntity);
                                scraperRepository.save(scraperEntity);
                            }
                        } catch (Exception e) {
                            log.error("Exception scraping property params.", e);
                        } finally {
                            driverWrapper.quit();
                        }
                    }
                }
            }
        }
    }
}
