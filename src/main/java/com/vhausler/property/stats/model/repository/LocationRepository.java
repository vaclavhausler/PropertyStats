package com.vhausler.property.stats.model.repository;

import com.vhausler.property.stats.model.entity.LocationEntity;
import jakarta.validation.constraints.NotNull;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LocationRepository extends CrudRepository<LocationEntity, String> {

    @Cacheable(value = "locationCache")
    @NotNull List<LocationEntity> findAll();
}
