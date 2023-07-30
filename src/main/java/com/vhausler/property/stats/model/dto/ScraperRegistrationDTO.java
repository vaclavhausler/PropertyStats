package com.vhausler.property.stats.model.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Data
@Validated
public class ScraperRegistrationDTO {

    @NotEmpty
    @ArraySchema(schema = @Schema(description = "List of scraper type ids.", example = "BYTY_PRODEJ"))
    private List<String> scraperTypeIds;
}
