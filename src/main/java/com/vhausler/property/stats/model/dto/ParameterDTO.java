package com.vhausler.property.stats.model.dto;

import lombok.Data;

@Data
public class ParameterDTO {
    private long id;
    private long scraperResultId;
    private String key;
    private String value;
}
