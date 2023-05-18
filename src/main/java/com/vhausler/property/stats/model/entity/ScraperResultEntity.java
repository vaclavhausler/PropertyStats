package com.vhausler.property.stats.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.sql.Timestamp;
import java.util.List;

@Data
@Entity
@Table(name = "scraper_result", schema = "property_stats", catalog = "property_stats")
public class ScraperResultEntity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ToString.Exclude
    @ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
    @JoinColumn(name = "scraper_id", referencedColumnName = "id")
    private ScraperEntity scraperEntity;

    @Basic
    @Column(name = "title")
    private String title;
    @Basic
    @Column(name = "address")
    private String address;
    @Basic
    @Column(name = "price")
    private Integer price;
    @Basic
    @Column(name = "price_per_square_meter")
    private Integer pricePerSquareMeter;
    @Basic
    @Column(name = "created")
    private Timestamp created;
    @Basic
    @Column(name = "link")
    private String link;

    @ToString.Exclude
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "scraperResult")
    private List<ParameterEntity> parameterEntities;
}
