package com.vhausler.property.stats.model.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "parameter", schema = "property_stats", catalog = "property_stats")
public class ParameterEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private int id;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "scraper_result_id", referencedColumnName = "id")
    private ScraperResultEntity scraperResult;

    @Basic
    @Column(name = "key")
    private String key;
    @Basic
    @Column(name = "value")
    private String value;
}
