package com.vhausler.property.stats.model.dto;

import lombok.Data;
import lombok.ToString;

import java.sql.Timestamp;
import java.util.List;

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
    private boolean available;
    @ToString.Exclude
    private List<ParameterDTO> parameterDTOS;
}
