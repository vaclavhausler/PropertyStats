package com.vhausler.property.stats.model.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "location")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = LocationEntity.class)
public class LocationEntity {

    @Id
    private String id;
    private String value;
    private boolean region;
    private boolean regionalCity;
    private boolean city;
}
