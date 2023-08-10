package com.vhausler.property.stats.model.mapper;

import com.vhausler.property.stats.model.dto.ScraperResultDTO;
import com.vhausler.property.stats.model.entity.ScraperResultEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class EntityMapperTest {

    @Autowired
    private EntityMapper entityMapper;

    /**
     * Tests {@link EntityMapper#scraperResultDTOToScraperResultEntity}.
     */
    @Test
    void testScraperResultDTOToScraperResultEntity() {
        // data
        ScraperResultDTO scraperResultDTO = new ScraperResultDTO();
        scraperResultDTO.setSquareMeters(0);

        // api call
        ScraperResultEntity scraperResultEntity = entityMapper.scraperResultDTOToScraperResultEntity(scraperResultDTO);

        // validation
        assertThat(scraperResultEntity.getSquareMeters()).isEqualTo(scraperResultDTO.getSquareMeters());
    }
}
