package com.vhausler.property.stats.config;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Configuration
public class ConfigProperties {

    @Bean
    @Validated
    @ConfigurationProperties("property-stats.webdriver")
    public WebDriverProperties webDriverProperties() {
        return new WebDriverProperties();
    }

    @Data
    public static class WebDriverProperties {
        @NotNull
        private Boolean headless;
        @NotNull
        private String pathToFirefoxExecutable;
    }
}
