package com.vhausler.property.stats.model.repository;

import com.vhausler.property.stats.model.entity.ScraperResultEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScraperResultRepository extends JpaRepository<ScraperResultEntity, Integer> {
    List<ScraperResultEntity> findAllByScraperEntity_idAndParamsDoneIsNull(long scraperId); // NOSONAR

    @Query(
            value = "select distinct on (link) * from property_stats.scraper_result where params_done is not null",
            countQuery = "select count(distinct link) from property_stats.scraper_result where params_done is not null",
            nativeQuery = true
    )
    Page<ScraperResultEntity> findDistinctLinks(Pageable pageable);

    List<ScraperResultEntity> findAllByScraperEntity_idAndLink(long id, String link);

    @Modifying
    @Query(nativeQuery = true,
            value = "DELETE FROM property_stats.scraper_result WHERE id IN (:scraperResultIds)")
    void deleteByIdIn(@Param("scraperResultIds") List<Long> scraperResultIds);

    List<ScraperResultEntity> findAllByParamsDoneIsNull();
}
