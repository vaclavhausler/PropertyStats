package com.vhausler.property.stats.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ScraperTypeDTO {
    @Schema(description = "Scraper type id, used to determine the scraper type.", example = "BYTY_PRODEJ")
    private String id;
    @Schema(description = "Scraper type search value used in actual scraping.", example = "prodej/byty")
    private String searchValue;
}
