package com.vhausler.property.stats.controller;

import com.vhausler.property.stats.model.dto.ScraperTypeDTO;
import com.vhausler.property.stats.service.SRealityService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequiredArgsConstructor
public class PropertyStatsController {

    private final SRealityService srealityService;

    @PostMapping("/register-scrapers/{scraperTypeId}")
    public void registerScrapers(@PathVariable String scraperTypeId) {
        srealityService.registerScrapers(scraperTypeId);
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

    @GetMapping("/scraper-types")
    public List<ScraperTypeDTO> getScraperTypes(){
        return srealityService.getScraperTypes();
    }
}
