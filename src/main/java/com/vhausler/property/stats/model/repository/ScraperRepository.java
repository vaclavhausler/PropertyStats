package com.vhausler.property.stats.model.repository;

import com.vhausler.property.stats.model.entity.LocationEntity;
import com.vhausler.property.stats.model.entity.ScraperEntity;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScraperRepository extends JpaRepository<ScraperEntity, Integer> {

    @Cacheable(value = "scraperEntityCache")
    List<ScraperEntity> findAllByLocationEntity(LocationEntity locationEntity);
}
