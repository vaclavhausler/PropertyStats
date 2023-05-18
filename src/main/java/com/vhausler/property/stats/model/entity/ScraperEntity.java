package com.vhausler.property.stats.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.sql.Timestamp;
import java.util.List;

@Data
@Entity
@Table(name = "scraper", schema = "property_stats", catalog = "property_stats")
public class ScraperEntity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.EAGER)
    @JoinColumn(name = "location_id", referencedColumnName = "id")
    private LocationEntity locationEntity;

    @Basic
    @Column(name = "created")
    private Timestamp created;
    @Basic
    @Column(name = "headers_done")
    private Timestamp headersDone;
    @Basic
    @Column(name = "params_done")
    private Timestamp paramsDone;

    @ToString.Exclude
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "scraperEntity")
    private List<ScraperResultEntity> scraperResultEntities;
}
