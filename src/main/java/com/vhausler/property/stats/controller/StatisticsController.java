package com.vhausler.property.stats.controller;

import com.vhausler.property.stats.model.dto.StatsDTO;
import com.vhausler.property.stats.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.vhausler.property.stats.model.Endpoints.STATS;

@RestController
@RequiredArgsConstructor
public class StatisticsController {

    private final StatsService statsService;

    @PostMapping(STATS)
    public StatsDTO getStats(@RequestParam String scraperTypeId) {
        return statsService.getStats(scraperTypeId);
    }
}
