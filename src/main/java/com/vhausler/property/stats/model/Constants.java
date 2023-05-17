package com.vhausler.property.stats.model;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

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
    }

    public static class CITY {
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
        public static final String PRAHA_1 = "praha-1";
        public static final String PRAHA_2 = "praha-2";
        public static final String PRAHA_3 = "praha-3";
        public static final String PRAHA_4 = "praha-4";
        public static final String PRAHA_5 = "praha-5";
        public static final String PRAHA_6 = "praha-6";
        public static final String PRAHA_7 = "praha-7";
        public static final String PRAHA_8 = "praha-8";
        public static final String PRAHA_9 = "praha-9";
        public static final String PRAHA_10 = "praha-10";
        public static final String BRNO = "brno";
        public static final String KARLOVY_VARY = "karlovy-vary";
        public static final String PLZEN = "plzen";
        public static final String USTI_NAD_LABEM = "usti-nad-labem";
        public static final String LIBEREC = "liberec";
        public static final String HRADEC_KRALOVE = "hradec-kralove";
        public static final String CESKE_BUDEJOVICE = "ceske-budejovice";
        public static final String JIHLAVA = "jihlava";
        public static final String PARDUBICE = "pardubice";
        public static final String OLOMOUC = "olomouc";
        public static final String ZLIN = "zlin";
        public static final String BENESOV = "benesov";

        public static List<String> getAll() {
            return Arrays.stream(CITY.class.getFields()).map(field -> {
                try {
                    return (String) field.get(null);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }).toList();
        }

        public static void main(String[] args) throws IllegalAccessException {
            String template = """
                            <insert tableName="location">
                                <column name="id" value="$1"/>
                                <column name="value" value="$2"/>
                                <column name="region" value=""/>
                                <column name="regional_city" value=""/>
                                <column name="city" value=""/>
                            </insert>
                    """;
            Field[] fields = CITY.class.getFields();
            for (Field field : fields) {
                String tmp = template.replace("$1", field.getName()).replace("$2", (String) field.get(null));
                System.out.println(tmp);
            }
        }
    }
}
