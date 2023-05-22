package com.vhausler.property.stats.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

@Data
@Entity
@Table(name = "parameter", schema = "property_stats", catalog = "property_stats")
public class ParameterEntity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ToString.Exclude
    @ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
    @JoinColumn(name = "scraper_result_id", referencedColumnName = "id")
    private ScraperResultEntity scraperResult;

    @Basic
    @Column(name = "key")
    private String key;
    @Basic
    @Column(name = "value")
    private String value;
}
