package com.vhausler.property.stats;

import org.openqa.selenium.firefox.FirefoxDriver;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class PropertyStatsApplication {
    public static void main(String[] args) {
        System.setProperty("webdriver.gecko.driver", "webdriver/geckodriver.exe"); // path to win32 gecko driver
        System.setProperty(FirefoxDriver.SystemProperty.BROWSER_LOGFILE, "/dev/null"); // turn off logging
        SpringApplication.run(PropertyStatsApplication.class, args);
    }
}
