package com.vhausler.property.stats.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "scraper_type", schema = "property_stats", catalog = "property_stats")
public class ScraperTypeEntity {

    @Id
    @Column
    private String id;

    @Column
    private String searchValue;
}
