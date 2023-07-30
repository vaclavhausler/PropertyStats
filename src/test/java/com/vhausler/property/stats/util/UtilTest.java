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
}