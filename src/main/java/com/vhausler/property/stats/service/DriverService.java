package com.vhausler.property.stats.service;

import com.vhausler.property.stats.config.ConfigProperties;
import com.vhausler.property.stats.model.DriverWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

import static com.vhausler.property.stats.util.Util.dealWithCookies;
import static com.vhausler.property.stats.util.Util.increaseResultSize;

@Slf4j
@Service
@RequiredArgsConstructor
public class DriverService {

    private final ConfigProperties.WebDriverProperties webDriverProperties;

    public DriverWrapper setupWebDriverGeneric(boolean headless) {
        return setupWebDriver(headless, null);
    }

    public DriverWrapper setupWebDriverSReality(boolean headless, String searchValue) {
        return setupWebDriver(headless, searchValue);
    }

    private DriverWrapper setupWebDriver(boolean headless, String searchValue) {
        Instant start = Instant.now();
        WebDriver wd;
        FirefoxOptions options = new FirefoxOptions();
        options.setBinary(webDriverProperties.getPathToFirefoxExecutable());
        if (headless) {
            options.addArguments("-headless");
        }

        wd = new FirefoxDriver(options);
        wd.manage().window().maximize();

        if (searchValue != null) {
            dealWithCookies(wd);
            increaseResultSize(wd, searchValue);
        }

        DriverWrapper driverWrapper = new DriverWrapper(wd);

        log.debug("New driver '{}' initialized in {} sec.", driverWrapper.getName(), Duration.between(start, Instant.now()).getSeconds());
        return driverWrapper;
    }

    public static void main(String[] args) {
        System.setProperty("webdriver.gecko.driver", "webdriver/geckodriver.exe"); // path to win32 gecko driver
        System.setProperty(FirefoxDriver.SystemProperty.BROWSER_LOGFILE, "/dev/null"); // turn off logging

        ConfigProperties.WebDriverProperties props = new ConfigProperties.WebDriverProperties();
        props.setHeadless(false);
        props.setPathToFirefoxExecutable("C:\\Program Files\\Mozilla Firefox\\firefox.exe");
        DriverService driverService = new DriverService(props);
        DriverWrapper driverWrapper = driverService.setupWebDriverSReality(false, "prodej/byty");
        WebDriver wd = driverWrapper.getWd();
        wd.get("https://www.sreality.cz/hledani/prodej/byty/pribram");
    }
}
