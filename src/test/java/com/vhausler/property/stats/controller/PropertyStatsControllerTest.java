package com.vhausler.property.stats.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vhausler.property.stats.model.dto.ScraperRegistrationDTO;
import com.vhausler.property.stats.service.SRealityService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.vhausler.property.stats.model.Endpoints.SCRAPER_REGISTRATION;
import static com.vhausler.property.stats.model.Endpoints.SCRAPER_TYPES;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test suite for {@link PropertyStatsController}.
 */
@WebMvcTest(controllers = PropertyStatsController.class)
class PropertyStatsControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private SRealityService service;

    /**
     * Tests {@link PropertyStatsController#registerScrapers(ScraperRegistrationDTO)}.
     */
    @Test
    void testRegisterScrapers() throws Exception {
        // data
        ScraperRegistrationDTO scraperRegistrationDTO = new ScraperRegistrationDTO();
        scraperRegistrationDTO.setScraperTypeIds(List.of("BYTY_PRODEJ", "BYTY_PRONAJEM"));

        // api call
        mvc.perform(post(SCRAPER_REGISTRATION)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(scraperRegistrationDTO))
        ).andExpect(status().isOk());

        // validation
        verify(service, times(1))
                .registerScrapers(scraperRegistrationDTO);
    }

    /**
     * Tests {@link PropertyStatsController#getScraperTypes()}.
     */
    @Test
    void testGetScraperTypes() throws Exception {
        // api call
        mvc.perform(get(SCRAPER_TYPES))
                .andExpect(status().isOk());

        // validation
        verify(service, times(1))
                .getScraperTypes();
    }
}
