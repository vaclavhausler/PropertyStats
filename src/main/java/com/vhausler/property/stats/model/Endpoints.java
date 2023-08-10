package com.vhausler.property.stats.model;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Endpoints {
    public static final String MAINTENANCE = "/maintenance";
    public static final String SCRAPER_TYPES = "/scraper-types";
    public static final String SCRAPER_REGISTRATION = "/scraper-registration";
}
