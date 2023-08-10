package com.vhausler.property.stats.model.repository;

import com.vhausler.property.stats.model.entity.ParameterEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParameterRepository extends CrudRepository<ParameterEntity, Long> {
    List<ParameterEntity> findAllByScraperResultEntity_id(Long scraperResultId);

    @Modifying
    @Query(nativeQuery = true,
            value = "DELETE FROM property_stats.parameter WHERE scraper_result_id IN (:scraperResultIds)")
    void deleteByScraperResultIdIn(@Param("scraperResultIds") List<Long> scraperResultIds);

    List<ParameterEntity> findAllByScraperResultEntity_IdIn(List<Long> scraperResultIds);
}
