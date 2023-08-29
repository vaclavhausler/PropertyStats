package com.vhausler.property.stats.controller;

import com.vhausler.property.stats.model.DriverWrapper;
import com.vhausler.property.stats.service.DriverService;
import com.vhausler.property.stats.util.Util;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PriceController {

    private final DriverService driverService;

    private final BigDecimal SMALL = BigDecimal.valueOf(1.5);
    private final BigDecimal BIG = BigDecimal.valueOf(2);
    private final BigDecimal SMALL_MULTI = BigDecimal.valueOf(6);

    @GetMapping("/coca-cola")
    public String getCocaColaPrices() {
        Instant start = Instant.now();
        StringBuilder result = new StringBuilder();
        String tescoUrl = "https://nakup.itesco.cz/groceries/cs-CZ/products/2001120538178";
        String tescoCssSelector = "[data-auto=\"price-value\"]";
        String tescoClubCardCssSelector = "[class=\"offer-text\"]";

        String rohlikUrl = "https://www.rohlik.cz/1437479-coca-cola";
        String rohlikCssSelector = "[data-test=\"product-price\"]";

        String kosikUrl = "https://www.kosik.cz/produkt/coca-cola-1-5l#productDescription";
        String kosikUrl2L = "https://www.kosik.cz/produkt/coca-cola-2l#productDescription";
        String kosikUrl4x15L = "https://www.kosik.cz/produkt/coca-cola-4x1-5l#productDescription";
        String kosikCssSelector = "[data-tid=\"product-box__price\"]";

        DriverWrapper driverWrapper = driverService.setupWebDriverGeneric(false);
        WebDriver wd = driverWrapper.getWd();

        addResult(result, wd, tescoUrl, tescoCssSelector, "Tesco (1.5 L)", SMALL);
        addResult(result, wd, tescoUrl, tescoClubCardCssSelector, "Tesco club card (1.5 L)", SMALL);
        addResult(result, wd, rohlikUrl, rohlikCssSelector, "Rohlík, (1.5 L)", SMALL);
        addResult(result, wd, kosikUrl, kosikCssSelector, "Košík (1.5 L)", SMALL);
        addResult(result, wd, kosikUrl2L, kosikCssSelector, "Košík (2 L)", BIG);
        addResult(result, wd, kosikUrl4x15L, kosikCssSelector, "Košík (4 x 1.5 L)", SMALL_MULTI);

        driverWrapper.quit();

        result.append("Results fetched in: ").append(Duration.between(start, Instant.now()).toSeconds()).append(" s.");
        return result.toString();
    }

    private void addResult(StringBuilder result, WebDriver wd, String url, String cssSelector, String companyName, BigDecimal amount) {
        if (!url.equals(wd.getCurrentUrl())) {
            wd.get(url);
        }
        Util.customWait(1000);
        WebElement priceElement = Util.waitUntilElementFound(wd, By.cssSelector(cssSelector));
        if (priceElement != null) {
            String priceText = priceElement.getText();
            String clubCardString = "S Clubcard ";
            if (priceText.contains(clubCardString)) {
                priceText = priceText.split(",")[0];
            }
            priceText = priceText.replace(clubCardString, "")
                    .replace(" ", "")
                    .replace("Kč", "");
            result.append(companyName).append(": ").append(priceText).append(" Kč");

            BigDecimal price = BigDecimal.valueOf(Double.parseDouble(priceText.replace(",", ".")));
            BigDecimal pricePerAmount = price.divide(amount, MathContext.DECIMAL32).setScale(2, RoundingMode.UP);

            result.append(" (").append(pricePerAmount).append(" / L)\n");

        } else {
            result.append(companyName).append(": price not found.\n");
        }
    }
}
