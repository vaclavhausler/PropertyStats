package com.vhausler.property.stats.service;

import com.vhausler.property.stats.model.dto.LocationDTO;
import com.vhausler.property.stats.model.entity.LocationEntity;
import com.vhausler.property.stats.model.mapper.EntityMapper;
import com.vhausler.property.stats.model.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SaleService {

    private final EntityMapper entityMapper;
    private final LocationRepository locationRepository;

    public List<LocationDTO> getAllLocations() {
        return entityMapper.locationEntitiesToLocationDTOs((List<LocationEntity>) locationRepository.findAll());
    }

    public void scrapeData() {
        List<LocationDTO> allLocations = getAllLocations();
        // go through all locations
        // check ScraperEntity for each of them and the current date, if it doesn't exist, create a new one and start header scraper for the location
        // if it exists for the current date, check if headers are done, if they are not done, start a header scraper for the location
        // if headers are done and parameters are not done, start a parameter scraper for the headers
        // if everything is done, skip
    }
}
