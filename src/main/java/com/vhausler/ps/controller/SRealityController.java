package com.vhausler.ps.controller;

import com.vhausler.ps.model.Constants;
import com.vhausler.ps.model.Property;
import com.vhausler.ps.util.Util;
import io.opentelemetry.api.internal.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Main runnable class which handles scraping of <a href="https://www.sreality.cz/">sreality.cz</a> website.
 */
public class SRealityController {
    private static final Logger LOGGER = LoggerFactory.getLogger(SRealityController.class);
    private WebDriver wd;

    public static void main(String[] args) {
        SRealityController controller = new SRealityController();
        controller.start();
    }

    /**
     * Main method which sets up the webdriver itself, prepares anything required for scraping like cookies, pagination setup and runs the scraper.
     */
    public void start() {
        // driver setup
        wd = new FirefoxDriver();

        // page properties
        dealWithCookies();
        increaseResultSize();

        // scrape data
        scrapeAndExport(Constants.URL.LITOMERICE);
    }

    /**
     * Scrapes the city url for any properties and exports them into an Excel file with the name of the city.
     *
     * @param cityURL like <a href="https://www.sreality.cz/hledani/prodej/byty/pribram">https://www.sreality.cz/hledani/prodej/byty/pribram</a>
     */
    @SuppressWarnings("SameParameterValue")
    private void scrapeAndExport(String cityURL) {
        List<Property> allProperties = process(cityURL);
        Util.exportResults(allProperties, cityURL);
    }

    /**
     * Scrapes the city url for any properties. Uses dynamic waiting to load the property list, understands the pagination and goes through all the pages.
     * Properties with no prices listed are skipped. Calculates price per square meter based on the square meters in the title and the price.
     *
     * @param cityURL like <a href="https://www.sreality.cz/hledani/prodej/byty/pribram">https://www.sreality.cz/hledani/prodej/byty/pribram</a>
     * @return list of {@link Property} found on the city url and any subsequent pages
     */
    private List<Property> process(String cityURL) {
        // check pagination
        LOGGER.debug("Navigating to {}.", cityURL);
        wd.get(cityURL);
        LOGGER.debug("Checking pagination.");
        waitUntilClassFound("property-list"); // NOSONAR
        int totalNumberOfProperties = Integer.parseInt(wd.findElement(By.cssSelector("div[paging='paging']")).findElement(By.cssSelector("span:last-of-type")).getText().replaceAll(" ", "")); // NOSONAR
        int numberOfPages = (int) Math.ceil((double) totalNumberOfProperties / (double) 60);
        LOGGER.debug("Found {} pages to go through.", numberOfPages);

        // scrape data
        List<Property> allProperties = new ArrayList<>();
        for (int i = 1; i <= numberOfPages; i++) {
            String currentCityURL = cityURL + Constants.URL.PAGINATION + i;
            LOGGER.debug("Navigating to {}.", currentCityURL);
            wd.get(currentCityURL);
            waitUntilClassFound("property-list");
            List<WebElement> properties = wd.findElements(By.className("property"));
            LOGGER.debug("Found {} properties.", properties.size());
            for (WebElement property : properties) {
                WebElement info = property.findElement(By.className("info"));
                String title = info.findElement(By.className("title")).getText().replaceAll(" ", " "); // NOSONAR
                Integer squareMeters = getSquareMeters(title);
                String address = info.findElement(By.className("locality")).getText().replaceAll(" ", " "); // NOSONAR
                Integer price = getPrice(info.findElement(By.className("price")).getText());
                Integer pricePerSquareMeter = squareMeters == null || price == null ? null : price / squareMeters;
                String link = info.findElement(By.cssSelector("a[class='title']")).getAttribute("href");
                if (price != null) {
                    allProperties.add(new Property(title, address, price, pricePerSquareMeter, new Date(), link));
                }
            }
        }

        return allProperties;
    }

    /**
     * Adjusts the result size from 20 to 60 per page. Persists through the whole scraping. It's possible that this value is stored in another cookie,
     * so there could be a workaround and a potential perf improvement for this.
     */
    private void increaseResultSize() {
        LOGGER.debug("Increasing result size.");

        // navigate to a site with results
        wd.get(Constants.URL.PRIBRAM);
        waitUntilClassFound("property-list");

        // increase the result size
        wd.findElement(By.className("per-page")).findElement(By.cssSelector("span[list-select='listSelectConf'")).click();
        WebElement perPageSelect = waitUntilClassFound("per-page-select");
        perPageSelect.findElement(By.className("options")).findElement(By.cssSelector("li:last-of-type")).findElement(By.cssSelector("button")).click();

        LOGGER.debug("Done increasing result size.");
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
    private WebElement waitUntilClassFound(String className) {
        LOGGER.debug("Waiting for element found by class name: '{}', to be present and to contain any text.", className);
        while (true) {
            WebElement element = wd.findElement(By.className(className));
            if (!StringUtils.isNullOrEmpty(element.getText())) {
                LOGGER.debug("Element with class name: '{}' found.", className);
                return element;
            }
            try {
                //noinspection BusyWait
                Thread.sleep(100);
            } catch (InterruptedException e) { // NOSONAR
                throw new IllegalStateException(e);
            }
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
    private void dealWithCookies() {
        LOGGER.debug("Dealing with cookies.");
        Cookie consentCookie = Util.getCookie(Constants.Cookie.CONSENT);
        LOGGER.trace("Navigating to domain.");
        wd.get(Constants.URL.BASE_URL + "/404");
        wd.manage().deleteAllCookies();
        LOGGER.trace("Adding cookie.");
        wd.manage().addCookie(consentCookie);
        LOGGER.debug("Done dealing with cookies.");
    }
}
