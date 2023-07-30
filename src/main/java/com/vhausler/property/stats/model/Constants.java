package com.vhausler.property.stats.model;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Model class which holds all the constants.
 */
public class Constants {
    private Constants() {
        throw new IllegalStateException("Class doesn't support instantiation."); // NOSONAR
    }

    public static final String RESULTS_FOLDER = "results";

    /**
     * Contains cookie related constants.
     */
    public static class Cookie {
        private Cookie() {
            throw new IllegalStateException("Class doesn't support instantiation.");
        }

        // this could've potentially been a reference to a json stored in resources
        @SuppressWarnings("SpellCheckingInspection")
        public static final String CONSENT = "{" +
                "\"name\":\"euconsent-v2\"," +
                "\"value\":\"CPluvIAPluvIAD3ACCCSCzCgAAAAAEPAAATIAAAQugTgALAAqABcADIAIAAZAA0ABzAEQARQAmABPACqAGIAPwAhIBEAESAI4ATgApQBYgDLAGaAO4AfoBCACLAFoALqAYEA1gBtAD5AJtAWoAvMBkgDSgGpgQuAAAAA.YAAAAAAAAAAA\"," +
                "\"path\":\"/\"," +
                "\"domain\":\".sreality.cz\"," +
                "\"expiry\":\"Feb 17, 2030, 2:40:09 PM\"," +
                "\"isSecure\":true," +
                "\"isHttpOnly\":false," +
                "\"sameSite\":\"None\"" +
                "}";
    }

    /**
     * Contains URL related cookies.
     */
    @SuppressWarnings("unused")
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class URL {

        public static final String PAGINATION = "?strana=";
        public static final String BASE_SETUP_URL = "https://www.sreality.cz/hledani/prodej/byty/";
        public static final String BASE_SEARCH_URL = "https://www.sreality.cz/hledani/${searchValue}/";

        /**
         * Leads to all property offers for the whole country.
         */
        public static final String ALL = ""; // = base URL
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class CITY {
        public static final String PRIBRAM = "pribram";
    }
}
