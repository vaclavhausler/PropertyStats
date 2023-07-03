package com.vhausler.property.stats.model.dto;

import lombok.Data;
import lombok.ToString;

import java.sql.Timestamp;
import java.util.List;

@Data
public class ScraperDTO {
    private int id;
    private String locationId;
    private Timestamp created;
    private Timestamp headersDone;
    private Timestamp paramsDone;
    @ToString.Exclude
    private List<ScraperResultDTO> scraperResultDTOS;
    private ScraperTypeDTO scraperTypeDTO;
}
