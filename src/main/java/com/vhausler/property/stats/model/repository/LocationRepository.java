package com.vhausler.property.stats.model.repository;

import com.vhausler.property.stats.model.entity.LocationEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LocationRepository extends CrudRepository<LocationEntity, String> {
}
