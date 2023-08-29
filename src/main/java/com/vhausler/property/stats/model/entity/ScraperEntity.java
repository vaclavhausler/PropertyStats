package com.vhausler.property.stats.model.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.sql.Timestamp;
import java.util.List;

@Data
@Entity
@Table(name = "scraper")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = ScraperEntity.class)
public class ScraperEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", referencedColumnName = "id")
    private LocationEntity locationEntity;

    private Timestamp created;
    private Timestamp headersDone;
    private Timestamp paramsDone;

    @ToString.Exclude
    @OneToMany(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY, mappedBy = "scraperEntity")
    private List<ScraperResultEntity> scraperResultEntities;

    @ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
    @JoinColumn(name = "scraper_type_id", referencedColumnName = "id")
    private ScraperTypeEntity scraperTypeEntity;
}
