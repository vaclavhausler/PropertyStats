package com.vhausler.property.stats.controller;

import com.vhausler.property.stats.config.TenantContext;
import com.vhausler.property.stats.model.dto.MaintenanceRequest;
import com.vhausler.property.stats.model.dto.ScraperRegistrationRequest;
import com.vhausler.property.stats.model.dto.ScraperTypeDTO;
import com.vhausler.property.stats.model.entity.ScraperEntity;
import com.vhausler.property.stats.service.SRealityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.vhausler.property.stats.model.Endpoints.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "Property Stats API")
public class PropertyStatsController {

    private final SRealityService srealityService;

    @Operation(summary = "Get all scraper types.", description = "Returns a list of all scraper types.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved.")
    @GetMapping(value = SCRAPER_TYPES, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ScraperTypeDTO> getScraperTypes() {
        return srealityService.getScraperTypes();
    }

    @Operation(summary = "Register scrapers.", description = "Registers new scrapers to be processed.")
    @ApiResponse(responseCode = "200", description = "Successfully registered.")
    @PostMapping(value = SCRAPER_REGISTRATION, consumes = MediaType.APPLICATION_JSON_VALUE)
    public void registerScrapers(@RequestBody @Valid ScraperRegistrationRequest scraperRegistrationRequest) {
        srealityService.registerScrapers(scraperRegistrationRequest);
    }

    @Operation(summary = "Starts a maintenance.", description = "Runs all maintenance operations.")
    @ApiResponse(responseCode = "200", description = "Successfully started.")
    @PostMapping(value = MAINTENANCE)
    public void runMaintenance(@RequestBody @Valid MaintenanceRequest maintenanceRequest) {
        srealityService.runMaintenance(maintenanceRequest);
    }

    @Operation(summary = "Starts a migration.", description = "Runs a data migration.")
    @ApiResponse(responseCode = "200", description = "Successfully started.")
    @PostMapping(value = MIGRATION)
    public void migrateData() {
        log.debug("Migration started.");
        Instant start = Instant.now();

        // switch to migration datasource
        TenantContext.setCurrentTenant(TenantContext.MIGRATION_TENANT);

        // load all scraper entities
        List<ScraperEntity> allScraperEntities = srealityService.fetchDataForMigration();

        // switch back to default datasource
        TenantContext.setCurrentTenant(TenantContext.DEFAULT_TENANT);

        // proceed with the migration
        srealityService.migrateData(allScraperEntities);

        log.debug("Migration done in: {}.", Duration.between(start, Instant.now()).toString());
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
