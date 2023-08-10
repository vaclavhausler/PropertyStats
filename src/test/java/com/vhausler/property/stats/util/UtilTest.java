package com.vhausler.property.stats.util;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test suite for {@link Util}.
 */
class UtilTest {

    /**
     * Tests {@link Util#getPrice(String)}.
     */
    @Test
    void testGetPrice() {
        // data
        List<Pair<String, Integer>> pairs = new ArrayList<>();
        pairs.add(Pair.of(" 13 000 Kč za měsíc ", 13000));

        // api call and validation
        for (Pair<String, Integer> pair : pairs) {
            Integer price = Util.getPrice(pair.getKey());
            assertThat(price).isEqualTo(pair.getRight());
        }
    }

    /**
     * Tests {@link Util#getSquareMeters(String)}.
     */
    @Test
    void testGetSquareMeters() {
        // data
        testGetSquareMeters("Prodej obchodního prostoru 6 002 m²", 6002);
        testGetSquareMeters("Prodej památky 1 008 m², pozemek 819 m²", 1827);
        testGetSquareMeters("Prodej podílu 11 770/100 000 bytu 3+kk 146 m² (Podkrovní).", 146);
        testGetSquareMeters("Prodej podílu 4 963/20 000 bytu 4+1 120 m²", 120);
        testGetSquareMeters("Prodej podílu 73/512 rybníku (vodní plochy) 3 094 m²", 3094);
        testGetSquareMeters("Prodej podílu 25/100 vícegeneračního domu 110 m², pozemek 743 m²", 853);
        testGetSquareMeters("Prodej podílu 1/8 stavebního pozemku 236 473 m²", 236473);
        testGetSquareMeters("Prodej louky 10 001 m²", 10001);
        testGetSquareMeters("Prodej podílu 26 102 419/100 296 888 vily 600 m², pozemek 463 m²", 10063);
    }

    private void testGetSquareMeters(String title, int expectedSquareMeters) {
        // api call
        Integer actualSquareMeters = Util.getSquareMeters(title);

        // validation
        assertThat(actualSquareMeters).isEqualTo(expectedSquareMeters);
    }
}