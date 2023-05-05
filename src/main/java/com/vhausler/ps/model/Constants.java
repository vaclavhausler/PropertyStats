package com.vhausler.ps.model;

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
        public static final String ALL = ""; // = base URL
        public static final String JIHOCESKY_KRAJ = "jihocesky-kraj";
        public static final String JIHOMORAVSKY_KRAJ = "jihomoravsky-kraj";
        public static final String KARLOVARSKY_KRAJ = "karlovarsky-kraj";
        public static final String KRALOVEHRADECKY_KRAJ = "kralovehradecky-kraj";
        public static final String LIBERECKY_KRAJ = "liberecky-kraj";
        public static final String MORAVSKOSLEZSKY_KRAJ = "moravskoslezsky-kraj";
        public static final String OLOMOUCKY_KRAJ = "olomoucky-kraj";
        public static final String PARDUBICKY_KRAJ = "pardubicky-kraj";
        public static final String PLZENSKY_KRAJ = "plzensky-kraj";
        public static final String STREDOCESKY_KRAJ = "stredocesky-kraj";
        public static final String USTECKY_KRAJ = "ustecky-kraj";
        public static final String VYSOCINA_KRAJ = "vysocina-kraj";
        public static final String ZLINSKY_KRAJ = "zlinsky-kraj";
        public static final String LITOMERICE = "litomerice";
        public static final String LOUNY = "louny";
        public static final String OSTRAVA = "ostrava";
        public static final String PISEK = "pisek";
        public static final String PRAHA = "praha";
        public static final String PRIBRAM = "pribram";
    }
}
