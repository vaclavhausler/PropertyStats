package com.vhausler.property.stats.model.dto;

import lombok.Data;

@Data
public class ParameterDTO {
    private int id;
    private int scraperResultId;
    private String key;
    private String value;
}
