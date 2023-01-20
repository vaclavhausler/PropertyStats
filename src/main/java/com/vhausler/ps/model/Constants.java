package com.vhausler.ps.model;

/**
 * Model class which holds all the constants.
 */
public class Constants {
    private Constants() {
        throw new IllegalStateException("Class doesn't support instantiation."); // NOSONAR
    }

    /**
     * Contains cookie related constants.
     */
    public static class Cookie {
        private Cookie() {
            throw new IllegalStateException("Class doesn't support instantiation.");
        }

        // this could've potentially been a reference to a json stored in resources
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
    public static class URL {
        private URL() {
            throw new IllegalStateException("Class doesn't support instantiation.");
        }

        public static final String PAGINATION = "?strana=";
        public static final String BASE_URL = "https://www.sreality.cz/hledani/prodej/byty/";

        /**
         * Leads to all property offers for the whole country.
         */
        public static final String ALL = BASE_URL + "";
        public static final String PRAHA = BASE_URL + "praha";
        public static final String PRIBRAM = BASE_URL + "pribram";
        public static final String OSTRAVA = BASE_URL + "ostrava";
        public static final String PISEK = BASE_URL + "pisek";
        public static final String LITOMERICE = BASE_URL + "litomerice";
    }
}
