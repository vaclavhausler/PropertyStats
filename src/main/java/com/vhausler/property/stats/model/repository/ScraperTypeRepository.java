package com.vhausler.property.stats.model.repository;

import com.vhausler.property.stats.model.entity.ScraperTypeEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScraperTypeRepository extends CrudRepository<ScraperTypeEntity, String> {
}
