package com.vhausler.property.stats.service;

import com.vhausler.property.stats.model.dto.StatsDTO;
import com.vhausler.property.stats.model.entity.ScraperTypeEntity;
import com.vhausler.property.stats.model.repository.ScraperRepository;
import com.vhausler.property.stats.model.repository.ScraperResultRepository;
import com.vhausler.property.stats.model.repository.ScraperTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StatsService {
    private final ScraperRepository scraperRepository;
    private final ScraperTypeRepository scraperTypeRepository;
    private final ScraperResultRepository scraperResultRepository;

    public StatsDTO getStats(String scraperTypeId) {
        validateScraperType(scraperTypeId);
        scraperRepository.findAllByScraperTypeEntity_Id(scraperTypeId);
        return null;
    }

    private void validateScraperType(String scraperType) {
        Optional<ScraperTypeEntity> scraperTypeOpt = scraperTypeRepository.findById(scraperType);
        if (scraperTypeOpt.isEmpty()) {
            throw new IllegalStateException(String.format("ScraperType %s not found.", scraperType));
        }
    }
}
