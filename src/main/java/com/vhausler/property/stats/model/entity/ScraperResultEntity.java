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
@Table(name = "scraper_result")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = ScraperResultEntity.class)
public class ScraperResultEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ToString.Exclude
    @ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)
    @JoinColumn(name = "scraper_id", referencedColumnName = "id")
    private ScraperEntity scraperEntity;

    private String title;
    private String link;
    private String address;
    private Integer price;
    private Integer squareMeters;
    private Integer pricePerSquareMeter;
    private Timestamp created;
    private Timestamp paramsDone;
    private boolean available;

    @ToString.Exclude
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "scraperResultEntity")
    private List<ParameterEntity> parameterEntities;
}
