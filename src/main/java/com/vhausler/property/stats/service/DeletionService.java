package com.vhausler.property.stats.service;

import com.vhausler.property.stats.model.entity.ScraperResultEntity;
import com.vhausler.property.stats.model.repository.ParameterRepository;
import com.vhausler.property.stats.model.repository.ScraperResultRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeletionService {
    private final ParameterRepository parameterRepository;
    private final ScraperResultRepository scraperResultRepository;

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void deleteScraperResult(ScraperResultEntity scraperResultEntity) {
        scraperResultRepository.delete(scraperResultEntity);
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void deleteScraperResult(List<ScraperResultEntity> scraperResultEntities) {
        List<Long> scraperResultIds = scraperResultEntities.stream().map(ScraperResultEntity::getId).toList();

        parameterRepository.deleteByScraperResultIdIn(scraperResultIds);
        scraperResultRepository.deleteByIdIn(scraperResultIds);

//        scraperResultRepository.deleteAll(scraperResultEntities);
    }
}
