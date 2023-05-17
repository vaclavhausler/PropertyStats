package com.vhausler.property.stats.model.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Timestamp;
import java.util.List;

@Data
@Entity
@Table(name = "scraper", schema = "property_stats", catalog = "property_stats")
public class ScraperEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private int id;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
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

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<ScraperResultEntity> scraperResultEntities;
}
