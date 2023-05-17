package com.vhausler.property.stats.model.dto;

import lombok.Data;

@Data
public class LocationDTO {
    private String id;
    private String value;
    private boolean region;
    private boolean regionalCity;
    private boolean city;
}
