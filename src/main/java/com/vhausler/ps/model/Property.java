package com.vhausler.ps.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;
import java.util.Map;

@Data
@AllArgsConstructor
public class Property {
    private String title;
    private String address;
    private Integer price;
    private Integer pricePerSquareMeter;
    private Date createdAt;
    private String link;
    private Map<String, String> params;
}
