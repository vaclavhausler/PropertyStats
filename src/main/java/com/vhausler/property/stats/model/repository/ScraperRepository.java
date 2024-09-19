package com.vhausler.property.stats.model.repository;

import com.vhausler.property.stats.model.entity.ScraperEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScraperRepository extends JpaRepository<ScraperEntity, Integer> {

    List<ScraperEntity> findAllByHeadersDoneIsNull();

    List<ScraperEntity> findAllByHeadersDoneIsNotNullAndParamsDoneIsNull();

    @Query("select se " +
            "from ScraperEntity se " +
            "left join fetch se.locationEntity " +
            "left join fetch se.scraperTypeEntity")
    List<ScraperEntity> fetchDataForMigration();

    List<ScraperEntity> findAllByScraperTypeEntity_Id(String scraperTypeId);
}
