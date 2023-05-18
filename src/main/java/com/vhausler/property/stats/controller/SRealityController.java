package com.vhausler.property.stats.controller;

import com.vhausler.property.stats.model.Constants;
import com.vhausler.property.stats.model.DriverWrapper;
import com.vhausler.property.stats.model.Property;
import com.vhausler.property.stats.util.Util;
import io.opentelemetry.api.internal.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static com.vhausler.property.stats.util.Util.*;
import static org.apache.logging.log4j.util.Strings.isEmpty;

/**
 * Main runnable class which handles scraping of <a href="https://www.sreality.cz/">sreality.cz</a> website.
 */
@Slf4j
public class SRealityController {

//    private List<DriverWrapper> drivers;
//
//    public static final boolean HEADLESS = false;
//    public static final long DRIVER_TIMEOUT_IN_SEC = 300000000;
//
//    public static void main(String[] args) {
//        System.setProperty(FirefoxDriver.SystemProperty.BROWSER_LOGFILE, "/dev/null"); // turn off logging
//
//        SRealityController controller = new SRealityController();
//        controller.start(HEADLESS, 1);
//    }
//
//    /**
//     * Main method which sets up the webdriver itself, prepares anything required for scraping like cookies, pagination setup and runs the scraper.
//     */
//    public void start(boolean headless, int driverCount) {
//        // drivers setup
//        log.debug("Starting {} drivers.", driverCount);
//        drivers = new ArrayList<>();
//        for (int i = 0; i < driverCount; i++) {
//            new Thread(() -> drivers.add(setupWebDriver(headless))).start();
//        }
//
//        // controls
//        boolean all = true;
//        boolean custom = false;
//        boolean kraje = false;
//
//        // scrape data
//        boolean start = false;
//        if (all) {
//            List<String> cities = Constants.CITY.getAll();
//            for (String city : cities) {
//                if (Constants.CITY.STREDOCESKY_KRAJ.equals(city)) {
//                    start = true;
//                }
//                if (start) {
//                    scrapeAndExport(getAvailableDriver(), city);
//                }
//            }
//            scrapeAndExport(getAvailableDriver(), Constants.URL.ALL);
//        }
//
//        if (custom) {
//            scrapeAndExport(getAvailableDriver(), Constants.CITY.PRIBRAM);
//            scrapeAndExport(getAvailableDriver(), Constants.CITY.OSTRAVA);
//            scrapeAndExport(getAvailableDriver(), Constants.CITY.LITOMERICE);
//            scrapeAndExport(getAvailableDriver(), Constants.CITY.PISEK);
//            scrapeAndExport(getAvailableDriver(), Constants.CITY.LOUNY);
//            scrapeAndExport(getAvailableDriver(), Constants.CITY.PRAHA);
//        }
//
//        // KRAJE
//        if (kraje) {
//            scrapeAndExport(getAvailableDriver(), Constants.CITY.JIHOCESKY_KRAJ);
//            scrapeAndExport(getAvailableDriver(), Constants.CITY.JIHOMORAVSKY_KRAJ);
//            scrapeAndExport(getAvailableDriver(), Constants.CITY.KARLOVARSKY_KRAJ);
//            scrapeAndExport(getAvailableDriver(), Constants.CITY.KRALOVEHRADECKY_KRAJ);
//            scrapeAndExport(getAvailableDriver(), Constants.CITY.LIBERECKY_KRAJ);
//            scrapeAndExport(getAvailableDriver(), Constants.CITY.MORAVSKOSLEZSKY_KRAJ);
//            scrapeAndExport(getAvailableDriver(), Constants.CITY.OLOMOUCKY_KRAJ);
//            scrapeAndExport(getAvailableDriver(), Constants.CITY.PARDUBICKY_KRAJ);
//            scrapeAndExport(getAvailableDriver(), Constants.CITY.PLZENSKY_KRAJ);
//            scrapeAndExport(getAvailableDriver(), Constants.CITY.STREDOCESKY_KRAJ);
//            scrapeAndExport(getAvailableDriver(), Constants.CITY.USTECKY_KRAJ);
//            scrapeAndExport(getAvailableDriver(), Constants.CITY.VYSOCINA_KRAJ);
//            scrapeAndExport(getAvailableDriver(), Constants.CITY.ZLINSKY_KRAJ);
//            scrapeAndExport(getAvailableDriver(), Constants.CITY.PRAHA_1);
//            scrapeAndExport(getAvailableDriver(), Constants.CITY.PRAHA_2);
//            scrapeAndExport(getAvailableDriver(), Constants.CITY.PRAHA_3);
//            scrapeAndExport(getAvailableDriver(), Constants.CITY.PRAHA_4);
//            scrapeAndExport(getAvailableDriver(), Constants.CITY.PRAHA_5);
//            scrapeAndExport(getAvailableDriver(), Constants.CITY.PRAHA_6);
//            scrapeAndExport(getAvailableDriver(), Constants.CITY.PRAHA_7);
//            scrapeAndExport(getAvailableDriver(), Constants.CITY.PRAHA_8);
//            scrapeAndExport(getAvailableDriver(), Constants.CITY.PRAHA_9);
//            scrapeAndExport(getAvailableDriver(), Constants.CITY.PRAHA_10);
//        }
//
//
//        // cleanup
//        for (DriverWrapper driverWrapper : drivers) {
//            if (driverWrapper.isAvailable()) {
//                driverWrapper.quit();
//            }
//        }
//    }
//
//    private DriverWrapper getAvailableDriver() {
//        for (DriverWrapper driverWrapper : drivers) {
//            if (driverWrapper.isAvailable()) {
//                if (drivers.size() > 1 && driverWrapper.isOutdated(DRIVER_TIMEOUT_IN_SEC)) {
//                    log.debug("Terminating outdated driver '{}'.", driverWrapper.getName());
//                    driverWrapper.quit();
//                    driverWrapper = setupWebDriver(HEADLESS);
//                }
//                driverWrapper.setAvailable(false);
//                return driverWrapper;
//            }
//        }
//        customWait(500);
//        return getAvailableDriver();
//    }

}
