package com.vhausler.property.stats.model.repository;

import com.vhausler.property.stats.model.entity.ScraperResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScraperResultRepository extends JpaRepository<ScraperResultEntity, Integer> {
}
