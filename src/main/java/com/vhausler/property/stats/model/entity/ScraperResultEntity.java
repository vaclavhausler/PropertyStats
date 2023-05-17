package com.vhausler.property.stats.model.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Timestamp;
import java.util.List;

@Data
@Entity
@Table(name = "scraper_result", schema = "property_stats", catalog = "property_stats")
public class ScraperResultEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private int id;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
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
    private int price;
    @Basic
    @Column(name = "price_per_square_meter")
    private int pricePerSquareMeter;
    @Basic
    @Column(name = "created")
    private Timestamp created;
    @Basic
    @Column(name = "link")
    private String link;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<ParameterEntity> parameterEntities;
}
