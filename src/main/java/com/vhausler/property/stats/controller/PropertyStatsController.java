package com.vhausler.property.stats.controller;

import com.vhausler.property.stats.model.dto.ScraperRegistrationDTO;
import com.vhausler.property.stats.model.dto.ScraperTypeDTO;
import com.vhausler.property.stats.service.SRealityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.vhausler.property.stats.model.Endpoints.SCRAPER_REGISTRATION;
import static com.vhausler.property.stats.model.Endpoints.SCRAPER_TYPES;

@RestController
@RequiredArgsConstructor
public class PropertyStatsController {

    private final SRealityService srealityService;

    @GetMapping(value = SCRAPER_TYPES, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ScraperTypeDTO> getScraperTypes() {
        return srealityService.getScraperTypes();
    }

    @PostMapping(value = SCRAPER_REGISTRATION, consumes = MediaType.APPLICATION_JSON_VALUE)
    public void registerScrapers(@RequestBody ScraperRegistrationDTO scraperRegistrationDTO) {
        srealityService.registerScrapers(scraperRegistrationDTO);
    }

    @Async
    @Scheduled(fixedRate = 15, timeUnit = TimeUnit.SECONDS)
    public void scrapeHeaders() {
        srealityService.scrapeHeaders();
    }

    @Async
    @Scheduled(fixedRate = 30, timeUnit = TimeUnit.SECONDS)
    public void scrapeParams() {
        srealityService.scrapeParams();
    }
}
