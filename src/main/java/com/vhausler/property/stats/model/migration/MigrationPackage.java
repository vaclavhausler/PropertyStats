package com.vhausler.property.stats.model.migration;

import com.vhausler.property.stats.model.entity.ScraperEntity;
import lombok.Data;

import java.util.List;

@Data
public class MigrationPackage {
    List<ScraperEntity> scraperEntities;
}
