package com.vhausler.property.stats.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class ScraperRegistrationDTO {
    private List<String> scraperTypeIds;
}
