package com.vhausler.property.stats.model;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

        public static final String REQUEST_COOKIES = """
                {
                \t"Request Cookies": {
                \t\t"__cw_snc": "1",
                \t\t"cmphitorder": "5",
                \t\t"cmprefreshcount": "4|2qegfs6qtk",
                \t\t"cmpsessid": "2qegfs6qtk",
                \t\t"cookie-wall-enabled": "1",
                \t\t"csrf-token": "jT5CS4iVk94=",
                \t\t"ds": "1YGAuASHow9cnsz6J7zShwdPfdZ_6q1Ymd6moLo3h3pTg8sw1YItIeOFDvzu5OZWR3Zcag",
                \t\t"euconsent-v2": "CP1pK0AP1pK0AD3ACBCSAwEsAP_gAEPgAATIJVwQQAAwAKAAsACAAFQALgAZAA6ACAAFAAKgAWgAyABoADmAIgAigBHACSAEwAJwAVQAtgBfgDCAMUAgACEgEQARQAjoBOAE6AL4AaQA4gB3ADxAH6AQgAkwBOACegFIAKyAWYAuoBgQDTgG0APkAjUBHQCaQE2gJ0AVIAtQBbgC8wGMgMkAZcA0oBqYDugHfgQHAhcBGYCVYIXQIoAFAAWABUAC4AIAAZAA0ACIAEcAJgAVQAxAB-AEJAIgAiQBHACcAGWAM0AdwA_QCEAEWALqAbQBNoCpAFqALcAXmAwQBkgDLgGpgQuAAAAA.YAAAAAAAAAAA",
                \t\t"ftxt": "hWAqdchwzB0ucJ5l9rL6",
                \t\t"lps": "eyJfZnJlc2giOmZhbHNlLCJfcGVybWFuZW50Ijp0cnVlfQ.Zhzxtg.z4bouPGoWI_6u6T8duCytNzQ-v4",
                \t\t"ps": "1YGAuASHow9cnsz6J7zShwdPfdZ_6q1Ymd6moLo3h3pTg8sw1YItIeOFDvzu5OZWR3Zcag",
                \t\t"qusnyQusny": "1",
                \t\t"sid": "id=12445982479517931410|t=1711969857.734|te=1713172916.624|c=1FD6837D38882F704F184890B4887F6B",
                \t\t"szncmpone": "1",
                \t\t"szncsr": "1713172916",
                \t\t"sznIsFullyCompatible": "1",
                \t\t"udid": "m5R2aJ9XOkscLLc5DQ-Wg3A_VlKzuXiW@1713172857376@1713172857376",
                \t\t"WeatherLocality": "PRAHA"
                \t}
                }""";

        public static List<org.openqa.selenium.Cookie> getCookies() {
            Gson gson = new Gson();
            String requestCookies = new JsonParser().parse(REQUEST_COOKIES)
                    .getAsJsonObject()
                    .get("Request Cookies")
                    .toString();

            Map<String, String> cookiesMap = gson.fromJson(requestCookies, Map.class);

            return cookiesMap.entrySet().stream()
                    .map(entry -> new org.openqa.selenium.Cookie(entry.getKey(), entry.getValue()))
                    .collect(Collectors.toList());
        }
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
        public static final String PRAHA = "praha";
    }
}
