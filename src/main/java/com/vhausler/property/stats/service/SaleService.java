package com.vhausler.property.stats.service;

import com.vhausler.property.stats.config.ConfigProperties;
import com.vhausler.property.stats.model.DriverWrapper;
import com.vhausler.property.stats.model.entity.LocationEntity;
import com.vhausler.property.stats.model.entity.ScraperEntity;
import com.vhausler.property.stats.model.entity.ScraperResultEntity;
import com.vhausler.property.stats.model.repository.LocationRepository;
import com.vhausler.property.stats.model.repository.ScraperRepository;
import com.vhausler.property.stats.util.Util;
import jakarta.transaction.Transactional;
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

    /*
    java.util.ConcurrentModificationException: null
	at java.base/java.util.ArrayList$Itr.checkForComodification(ArrayList.java:1013) ~[na:na]
	at java.base/java.util.ArrayList$Itr.next(ArrayList.java:967) ~[na:na]
	at org.hibernate.collection.spi.AbstractPersistentCollection$IteratorProxy.next(AbstractPersistentCollection.java:917) ~[hibernate-core-6.1.7.Final.jar:6.1.7.Final]
	at com.vhausler.property.stats.service.SaleService.scrapeParams(SaleService.java:92) ~[classes/:na]
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method) ~[na:na]
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:77) ~[na:na]
	at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43) ~[na:na]
	at java.base/java.lang.reflect.Method.invoke(Method.java:568) ~[na:na]

	Entity -> DTO -> Entity to prevent concurrent modifications?
     */

    @Transactional
    public void registerScrapers() {
        log.trace("Scheduler: registerScrapers. START.");
        List<LocationEntity> allLocationEntities = locationRepository.findAll();
        log.trace("Locations found: {}.", allLocationEntities.size());
        for (LocationEntity locationEntity : allLocationEntities) {
            List<ScraperEntity> allByLocationEntity = scraperRepository.findAllByLocationEntity(locationEntity);
            log.trace("Found {} scraper entities for location: {}.", allByLocationEntity.size(), locationEntity.getId());
            if (allByLocationEntity.isEmpty()) {
                // create new scraper entity
                ScraperEntity scraperEntity = new ScraperEntity();
                scraperEntity.setLocationEntity(locationEntity);
                log.debug("Creating scraper entity: {}.", scraperEntity);
                scraperEntity.setCreated(getCurrentTimestamp());
                scraperRepository.save(scraperEntity);
            }
        }
        log.trace("Scheduler: registerScrapers. DONE.");
    }

    @Transactional
    public void scrapeHeaders() {
        log.debug("Scheduler: scrapeHeaders. START.");
        List<LocationEntity> allLocationEntities = locationRepository.findAll();
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
        log.debug("Scheduler: scrapeHeaders. DONE.");
    }

    @Transactional
    public void scrapeParams() { // NOSONAR
        log.debug("Scheduler: scrapeParams. START.");
        List<LocationEntity> allLocationEntities = locationRepository.findAll();
        for (LocationEntity locationEntity : allLocationEntities) {
            List<ScraperEntity> allByLocationEntity = scraperRepository.findAllByLocationEntity(locationEntity);
            if (!allByLocationEntity.isEmpty()) {
                // process anything unfinished
                for (ScraperEntity scraperEntity : allByLocationEntity) {
                    if (scraperEntity.getParamsDone() == null) {
                        log.debug("Scraping params for scraper entity: {}.", scraperEntity);
                        DriverWrapper driverWrapper = driverService.setupWebDriver(webDriverProperties.getHeadless());
                        try {
                            int done = 0;
                            for (ScraperResultEntity scraperResultEntity : scraperEntity.getScraperResultEntities()) {
                                if (scraperResultEntity.getParameterEntities().isEmpty()) {
                                    Util.scrapePropertyParams(driverWrapper, scraperResultEntity);
                                    scraperRepository.save(scraperEntity);
                                }
                                if (done++ % 20 == 0) {
                                    log.debug("Finished scraping property params for {}/{} properties for {}.", done, scraperEntity.getScraperResultEntities().size(), scraperEntity.getLocationEntity().getId());
                                }
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
        log.debug("Scheduler: scrapeParams. DONE.");
    }
}
