package com.vhausler.property.stats.controller;

import com.vhausler.property.stats.service.SaleService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
@RequiredArgsConstructor
public class PropertyStatsController {

    private final SaleService saleService;

    @Async
    @Scheduled(fixedRate = 5, timeUnit = TimeUnit.SECONDS)
    public void registerScrapers() {
        saleService.registerScrapers();
    }

    @Async
    @Scheduled(fixedRate = 5, timeUnit = TimeUnit.SECONDS)
    public void scrapeHeaders() {
        saleService.scrapeHeaders();
    }

    @Async
    @Scheduled(fixedRate = 5, timeUnit = TimeUnit.SECONDS)
    public void scrapeParams() {
        saleService.scrapeParams();
    }
}
