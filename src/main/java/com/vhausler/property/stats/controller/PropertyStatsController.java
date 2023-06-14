package com.vhausler.property.stats.controller;

import com.vhausler.property.stats.service.SRealityService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
@RequiredArgsConstructor
public class PropertyStatsController {

    private final SRealityService SRealityService;

    @PostMapping("/register-scrapers")
    public void registerScrapers() {
        SRealityService.registerScrapers();
    }

    @Async
    @Scheduled(fixedRate = 5, timeUnit = TimeUnit.SECONDS)
    public void scrapeHeaders() {
        SRealityService.scrapeHeaders();
    }

    @Async
    @Scheduled(fixedRate = 5, timeUnit = TimeUnit.SECONDS)
    public void scrapeParams() {
        SRealityService.scrapeParams();
    }
}
