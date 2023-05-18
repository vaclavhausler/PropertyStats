package com.vhausler.property.stats.model.repository;

import com.vhausler.property.stats.model.entity.LocationEntity;
import com.vhausler.property.stats.model.entity.ScraperEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScraperRepository extends CrudRepository<ScraperEntity, Integer> {

    List<ScraperEntity> findAllByLocationEntity(LocationEntity locationEntity);
}
