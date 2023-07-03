package com.vhausler.property.stats.model.repository;

import com.vhausler.property.stats.model.entity.ParameterEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParameterRepository extends CrudRepository<ParameterEntity, Long> {
    List<ParameterEntity> findAllByScraperResultEntity_id(Long scraperResultId);
}
