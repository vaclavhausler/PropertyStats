package com.vhausler.property.stats.model.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
@Table(name = "location", schema = "property_stats", catalog = "property_stats")
public class LocationEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private String id;
    @Basic
    @Column(name = "value")
    private String value;
    @Basic
    @Column(name = "region")
    private boolean region;
    @Basic
    @Column(name = "regional_city")
    private boolean regionalCity;
    @Basic
    @Column(name = "city")
    private boolean city;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<ScraperEntity> scraperEntities;
}
