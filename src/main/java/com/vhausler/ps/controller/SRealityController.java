package com.vhausler.ps.controller;

import com.vhausler.ps.model.Constants;
import com.vhausler.ps.model.DriverWrapper;
import com.vhausler.ps.model.Property;
import com.vhausler.ps.util.Util;
import io.opentelemetry.api.internal.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

import static com.vhausler.ps.model.Constants.CITY.*;
import static com.vhausler.ps.model.Constants.URL.ALL;
import static com.vhausler.ps.model.Constants.URL.BASE_URL;
import static org.apache.logging.log4j.util.Strings.isEmpty;

/**
 * Main runnable class which handles scraping of <a href="https://www.sreality.cz/">sreality.cz</a> website.
 */
@Slf4j
public class SRealityController {

    private List<DriverWrapper> drivers;

    public static final boolean HEADLESS = false;
    public static final long DRIVER_TIMEOUT_IN_SEC = 30;

    public static void main(String[] args) {
        System.setProperty(FirefoxDriver.SystemProperty.BROWSER_LOGFILE, "/dev/null"); // turn off logging

        SRealityController controller = new SRealityController();
        controller.start(HEADLESS, 1);
    }

    /**
     * Main method which sets up the webdriver itself, prepares anything required for scraping like cookies, pagination setup and runs the scraper.
     */
    public void start(boolean headless, int driverCount) {
        // drivers setup
        log.debug("Starting {} drivers.", driverCount);
        drivers = new ArrayList<>();
        for (int i = 0; i < driverCount; i++) {
            new Thread(() -> drivers.add(setupWebDriver(headless))).start();
        }

        // controls
        boolean all = true;
        boolean custom = false;
        boolean kraje = false;

        // scrape data
        boolean start = false;
        if (all) {
            List<String> cities = getAll();
            for (String city : cities) {
                if (KARLOVARSKY_KRAJ.equals(city)) {
                    start = true;
                }
                if (start) {
                    scrapeAndExport(getAvailableDriver(), city);
                }
            }
            scrapeAndExport(getAvailableDriver(), ALL);
        }

        if (custom) {
            scrapeAndExport(getAvailableDriver(), PRIBRAM);
            scrapeAndExport(getAvailableDriver(), OSTRAVA);
            scrapeAndExport(getAvailableDriver(), LITOMERICE);
            scrapeAndExport(getAvailableDriver(), PISEK);
            scrapeAndExport(getAvailableDriver(), LOUNY);
            scrapeAndExport(getAvailableDriver(), PRAHA);
        }

        // KRAJE
        if (kraje) {
            scrapeAndExport(getAvailableDriver(), JIHOCESKY_KRAJ);
            scrapeAndExport(getAvailableDriver(), JIHOMORAVSKY_KRAJ);
            scrapeAndExport(getAvailableDriver(), KARLOVARSKY_KRAJ);
            scrapeAndExport(getAvailableDriver(), KRALOVEHRADECKY_KRAJ);
            scrapeAndExport(getAvailableDriver(), LIBERECKY_KRAJ);
            scrapeAndExport(getAvailableDriver(), MORAVSKOSLEZSKY_KRAJ);
            scrapeAndExport(getAvailableDriver(), OLOMOUCKY_KRAJ);
            scrapeAndExport(getAvailableDriver(), PARDUBICKY_KRAJ);
            scrapeAndExport(getAvailableDriver(), PLZENSKY_KRAJ);
            scrapeAndExport(getAvailableDriver(), STREDOCESKY_KRAJ);
            scrapeAndExport(getAvailableDriver(), USTECKY_KRAJ);
            scrapeAndExport(getAvailableDriver(), VYSOCINA_KRAJ);
            scrapeAndExport(getAvailableDriver(), ZLINSKY_KRAJ);
            scrapeAndExport(getAvailableDriver(), PRAHA_1);
            scrapeAndExport(getAvailableDriver(), PRAHA_2);
            scrapeAndExport(getAvailableDriver(), PRAHA_3);
            scrapeAndExport(getAvailableDriver(), PRAHA_4);
            scrapeAndExport(getAvailableDriver(), PRAHA_5);
            scrapeAndExport(getAvailableDriver(), PRAHA_6);
            scrapeAndExport(getAvailableDriver(), PRAHA_7);
            scrapeAndExport(getAvailableDriver(), PRAHA_8);
            scrapeAndExport(getAvailableDriver(), PRAHA_9);
            scrapeAndExport(getAvailableDriver(), PRAHA_10);
        }


        // cleanup
        for (DriverWrapper driverWrapper : drivers) {
            if (driverWrapper.isAvailable()) {
                driverWrapper.quit();
            }
        }
    }

    private DriverWrapper getAvailableDriver() {
        for (DriverWrapper driverWrapper : drivers) {
            if (driverWrapper.isAvailable()) {
                if (drivers.size() > 1 && driverWrapper.isOutdated(DRIVER_TIMEOUT_IN_SEC)) {
                    log.debug("Terminating outdated driver '{}'.", driverWrapper.getName());
                    driverWrapper.quit();
                    driverWrapper = setupWebDriver(HEADLESS);
                }
                driverWrapper.setAvailable(false);
                return driverWrapper;
            }
        }
        customWait(500);
        return getAvailableDriver();
    }

    private DriverWrapper setupWebDriver(boolean headless) {
        Instant start = Instant.now();
        WebDriver wd;
        FirefoxOptions options = new FirefoxOptions();
        if (headless) {
            options.addArguments("-headless");
        }

        wd = new FirefoxDriver(options);
        wd.manage().window().maximize();

        dealWithCookies(wd);
        increaseResultSize(wd);
        DriverWrapper driverWrapper = new DriverWrapper(wd);

        log.debug("New driver '{}' initialized in {} sec.", driverWrapper.getName(), Duration.between(start, Instant.now()).getSeconds());
        return driverWrapper;
    }

    /**
     * Scrapes the city url for any properties and exports them into an Excel file with the name of the city.
     *
     * @param cityURL like <a href="https://www.sreality.cz/hledani/prodej/byty/pribram">https://www.sreality.cz/hledani/prodej/byty/pribram</a>
     */
    @SuppressWarnings("SameParameterValue")
    private void scrapeAndExport(DriverWrapper driverWrapper, String cityURL) {
        Instant start = Instant.now();
        List<Property> allProperties = scrapeProperties(driverWrapper, BASE_URL + cityURL);
        Util.exportResults(allProperties, cityURL);
        Instant stop = Instant.now();
        Duration dur = Duration.between(start, stop);
        log.debug("Finished processing {} in {} min.", cityURL, dur.toMinutes());
    }

    /**
     * Scrapes the city url for any properties. Uses dynamic waiting to load the property list, understands the pagination and goes through all the pages.
     * Properties with no prices listed are skipped. Calculates price per square meter based on the square meters in the title and the price.
     *
     * @param cityURL like <a href="https://www.sreality.cz/hledani/prodej/byty/pribram">https://www.sreality.cz/hledani/prodej/byty/pribram</a>
     * @return list of {@link Property} found on the city url and any subsequent pages
     */
    private List<Property> scrapeProperties(DriverWrapper driverWrapper, String cityURL) {
        WebDriver wd = driverWrapper.getWd();
        // check pagination
        log.debug("Navigating to {}.", cityURL);
        wd.get(cityURL);
        log.trace("Checking pagination.");
        waitUntilClassFound(wd, "property-list"); // NOSONAR
        int totalNumberOfProperties = Integer.parseInt(wd.findElement(By.cssSelector("div[paging='paging']")).findElement(By.cssSelector("span:last-of-type")).getText().replaceAll(" ", "")); // NOSONAR
        int totalNumberOfPages = (int) Math.ceil((double) totalNumberOfProperties / (double) 60);
        log.debug("Found {} pages to go through.", totalNumberOfPages);
        driverWrapper.setAvailable(true);

        // get all pages we need to scrape
        List<String> allPageLinks = new ArrayList<>();
        for (int i = 1; i <= totalNumberOfPages; i++) {
            allPageLinks.add(cityURL + Constants.URL.PAGINATION + i);
        }

        // scrape page data
        List<Property> allProperties = new ArrayList<>();
//        List<CompletableFuture<Void>> propertyFutures = new ArrayList<>();
        for (int i = 0; i < allPageLinks.size(); i++) {
            String pageLink = allPageLinks.get(i);
//            int finalI = i;
//            propertyFutures.add(CompletableFuture.runAsync(() -> scrapePageData(getAvailableDriver(), pageLink, finalI, totalNumberOfPages, allProperties)));
            scrapePageData(driverWrapper, pageLink, i, totalNumberOfPages, allProperties);
        }
//        propertyFutures.forEach(CompletableFuture::join);

        // scrape property parameters
        log.debug("Going through {} properties to fetch their parameters.", allProperties.size());
//        List<CompletableFuture<Void>> propertyParamsFutures = new ArrayList<>();
        int done = 0;
        for (Property property : allProperties) {
//            propertyParamsFutures.add(CompletableFuture.runAsync(() -> scrapePropertyParams(getAvailableDriver(), property)));
            scrapePropertyParams(driverWrapper, property);
            done++;
            if (done % 100 == 0) {
                log.debug("Finished scraping {}/{} property params for {}.", done, allProperties.size(), cityURL);
            }
        }
        log.debug("Finished scraping {}/{} property params for {}.", done, allProperties.size(), cityURL);
//        propertyParamsFutures.forEach(CompletableFuture::join);

        return allProperties;
    }

    private void scrapePageData(DriverWrapper driverWrapper, String pageLink, int index, int numberOfPages, List<Property> allProperties) {
        log.trace("Navigating to {}.", pageLink);
        WebDriver wd = driverWrapper.getWd();
        wd.get(pageLink);
        waitUntilClassFound(wd, "property-list");
        List<WebElement> properties = wd.findElements(By.className("property"));
        if (properties.isEmpty()) {
            log.debug("Found 0 properties, re-scraping page data.");
            scrapePageData(driverWrapper, pageLink, index, numberOfPages, allProperties);
            return;
        }
        log.debug("Found {} properties on page {}/{}: {}.", properties.size(), index + 1, numberOfPages, pageLink);
        for (WebElement property : properties) {
            WebElement info = property.findElement(By.className("info"));
            String title = info.findElement(By.className("title")).getText().replaceAll(" ", " "); // NOSONAR
            Integer squareMeters = getSquareMeters(title);
            String address = info.findElement(By.className("locality")).getText().replaceAll(" ", " "); // NOSONAR
            Integer price = getPrice(info.findElement(By.className("price")).getText());
            Integer pricePerSquareMeter = squareMeters == null || price == null ? null : price / squareMeters;
            String link = info.findElement(By.cssSelector("a[class='title']")).getAttribute("href");
            if (price != null) {
                allProperties.add(new Property(title, address, price, pricePerSquareMeter, new Date(), link, new HashMap<>()));
            }
        }
        driverWrapper.setAvailable(true);
    }

    private void scrapePropertyParams(DriverWrapper driverWrapper, Property property) {
        try {
            // extra params on property detail
            WebDriver wd = driverWrapper.getWd();
            wd.get(property.getLink());
            WebElement propertyTitleElement = waitUntilElementFound(wd, By.cssSelector("div[class='property-title']"));
            if (propertyTitleElement != null) {
                String propertyTitle = propertyTitleElement.getText();
                while (!(propertyTitle.contains(property.getTitle()) && propertyTitle.contains(property.getAddress()))) {
                    customWait(100);
                    log.debug("{}", propertyTitle);
                }
                WebElement propertyDetail = waitUntilElementFound(wd, By.cssSelector("div[class='params clear']"));
                if (propertyDetail != null) {
                    WebElement params1 = waitUntilClassFound(wd, "params1");
                    WebElement params2 = waitUntilClassFound(wd, "params2");
                    Map<String, String> propertyParams = new HashMap<>();
                    propertyParams.putAll(scrapeParams(params1));
                    propertyParams.putAll(scrapeParams(params2));
                    property.setParams(propertyParams);
                }
            }
        } catch (Exception e) {
            log.trace("Exception scraping property params from link: {}. {}.", property.getLink(), e.getMessage());
            scrapePropertyParams(driverWrapper, property);
        }
        log.trace("Finished scraping property params from link: {}.", property.getLink());
        driverWrapper.setAvailable(true);
    }

    private void customWait(long timeInMillis) {
        try {
            Thread.sleep(timeInMillis);
        } catch (InterruptedException e) {
            // ignore
        }
    }

    private Map<String, String> scrapeParams(WebElement params) {
        Map<String, String> result = new HashMap<>();
        List<WebElement> paramParents = params.findElements(By.cssSelector("li"));
        for (WebElement paramParent : paramParents) {
            String key = paramParent.findElement(By.cssSelector("label")).getText();
            if (key.contains(":")) {
                key = key.replace(":", "");
            }
            WebElement strong = paramParent.findElement(By.cssSelector("strong"));
            String value = strong.getText();
            if (isEmpty(value)) {
                // most likely boolean value
                WebElement icon = strong.findElement(By.className("icof"));
                String booleanAttribute = icon.getAttribute("ng-if");
                if ("item.type == 'boolean-false'".equals(booleanAttribute)) {
                    value = "false";
                } else if ("item.type == 'boolean-true'".equals(booleanAttribute)) {
                    value = "true";
                } else {
                    log.error("Expected a boolean attribute, but was something else: '{}'", booleanAttribute);
                }
            }
            result.put(key, value);
        }
        return result;
    }

    /**
     * Adjusts the result size from 20 to 60 per page. Persists through the whole scraping. It's possible that this value is stored in another cookie,
     * so there could be a workaround and a potential perf improvement for this.
     */
    private void increaseResultSize(WebDriver wd) {
        log.trace("Increasing result size.");

        // navigate to a site with results
        wd.get(BASE_URL + PRIBRAM);
        waitUntilClassFound(wd, "property-list");

        // increase the result size
        wd.findElement(By.className("per-page")).findElement(By.cssSelector("span[list-select='listSelectConf'")).click();
        WebElement perPageSelect = waitUntilClassFound(wd, "per-page-select");
        perPageSelect.findElement(By.className("options")).findElement(By.cssSelector("li:last-of-type")).findElement(By.cssSelector("button")).click();

        log.trace("Done increasing result size.");
    }

    /**
     * Attempts to parse the square meters value from the title of the property. Has to be able to handle different types of whitespaces.
     *
     * @param title e.g. Prodej bytu 2+1 70 m²
     * @return parsed square meters value or null if none found
     */
    private Integer getSquareMeters(String title) {
        title = title.replaceAll(" ", " "); // NOSONAR
        if (title.contains("m²")) {
            String[] split = title.split("m²")[0].split(" ");
            String strValue = split[split.length - 1];
            if (!StringUtils.isNullOrEmpty(strValue)) {
                return Integer.parseInt(strValue);
            }
        }
        return null;
    }

    /**
     * Dynamically waits until an element with a specific class name shows up on the page. Solves the issue of async data loading.
     * Workaround for {@link WebDriverWait} which I could never rely on. Intentionally set up as an endless loop in case anything breaks,
     * as the scraper would keep waiting and dev can easily debug the issue as they still have access to the page and see what happened.
     *
     * @param className used for lookup
     * @return an element found by its class name
     */
    private WebElement waitUntilClassFound(WebDriver wd, String className) {
        return waitUntilElementFound(wd, By.className(className));
    }

    private WebElement waitUntilElementFound(WebDriver wd, By by) {
        log.trace("Waiting for element found by: '{}', to be present and to contain any text.", by);
        while (true) {
            try {
                WebElement element = wd.findElement(by);
                if (!StringUtils.isNullOrEmpty(element.getText())) {
                    log.trace("Element '{}' found.", by);
                    return element;
                }
            } catch (Exception e) {
                // ignore not found exception
                try {
                    String errorDescription = wd.findElement(By.className("error-description")).getText();
                    if ("Je mi líto, inzerát neexistuje.".equals(errorDescription)) {
                        return null;
                    }
                } catch (Exception e2) {
                    // ignore not found exception
                }
                log.trace("Element {} not found, waiting.", by);
                wd.navigate().refresh();
                customWait(100);
            }
            customWait(100);
        }
    }

    /**
     * Attempts to parse the price from the string.
     *
     * @param price e. g. 2 750 000 Kč
     * @return the parsed price from the string or null in case anything fails
     */
    private Integer getPrice(String price) {
        Integer result;
        try {
            result = Integer.parseInt(price.replaceAll(" ", "").replaceAll("Kč", "")); // NOSONAR
        } catch (Exception e) {
            result = null;
        }
        return result;
    }

    /**
     * Workaround for GDPR consent via a cookie. Goes to a non-existing website on the domain which we need to add the cookie for
     * avoiding ugly redirect to <a href="https://www.seznam.cz">seznam.cz</a> where the actual consent form is located.
     */
    private void dealWithCookies(WebDriver wd) {
        log.trace("Dealing with cookies.");
        Cookie consentCookie = Util.getCookie(Constants.Cookie.CONSENT);
        log.trace("Navigating to domain.");
        wd.get(BASE_URL + "/404");
        customWait(2000);
        wd.get(BASE_URL + "/404");
        waitUntilElementFound(wd, By.className("error-description"));
        wd.manage().deleteAllCookies();
        log.trace("Adding cookie.");
        wd.manage().addCookie(consentCookie);
        log.trace("Done dealing with cookies.");
    }
}
