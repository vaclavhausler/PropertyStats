package com.vhausler.property.stats.model.dto;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class ScraperDTO {
    private int id;
    private String locationId;
    private Timestamp created;
    private Timestamp headersDone;
    private Timestamp paramsDone;
}
