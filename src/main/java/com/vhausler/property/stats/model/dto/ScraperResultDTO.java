package com.vhausler.property.stats.model.dto;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class ScraperResultDTO {
    private int id;
    private int scraperId;
    private String title;
    private String address;
    private int price;
    private int pricePerSquareMeter;
    private Timestamp created;
    private String link;
}
