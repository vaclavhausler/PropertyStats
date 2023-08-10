package com.vhausler.property.stats.util;

import com.google.gson.Gson;
import com.vhausler.property.stats.model.Constants;
import com.vhausler.property.stats.model.DriverWrapper;
import com.vhausler.property.stats.model.Property;
import com.vhausler.property.stats.model.dto.ParameterDTO;
import com.vhausler.property.stats.model.dto.ScraperDTO;
import com.vhausler.property.stats.model.dto.ScraperResultDTO;
import io.opentelemetry.api.internal.StringUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.apache.logging.log4j.util.Strings.isEmpty;

/**
 * Kitchen sink class for any utility methods.
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Util {
    private static final Logger LOGGER = LoggerFactory.getLogger(Util.class);
    public static final String NO_OFFER_FOUND_EXCEPTION = "no-offer-found-exception";

    /**
     * Scrapes the city url for any properties. Uses dynamic waiting to load the property list, understands the pagination and goes through all the pages.
     * Properties with no prices listed are skipped. Calculates price per square meter based on the square meters in the title and the price.
     *
     * @param scraperDTO          contains link - cityURL like <a href="https://www.sreality.cz/hledani/prodej/byty/pribram">https://www.sreality.cz/hledani/prodej/byty/pribram</a>
     * @param locationEntityValue for scraper link creation
     */
    public static void scrapePropertyHeaders(DriverWrapper driverWrapper, ScraperDTO scraperDTO, String locationEntityValue) {
        WebDriver wd = driverWrapper.getWd();
        // check pagination
        String cityURL = Constants.URL.BASE_SEARCH_URL.replace("${searchValue}", scraperDTO.getScraperTypeDTO().getSearchValue()) + locationEntityValue;
        log.debug("{}: Navigating to {}.", driverWrapper.getName(), cityURL);
        wd.get(cityURL);
        customWait(500);
        log.trace("{}: Checking pagination.", driverWrapper.getName());
        waitUntilElementFound(wd, By.className("property-list")); // NOSONAR
        int totalNumberOfPages;
        try {
            int totalNumberOfProperties = Integer.parseInt(wd.findElement(By.cssSelector("div[paging='paging']")).findElement(By.cssSelector("span:last-of-type")).getText().replaceAll(" ", "")); // NOSONAR
            totalNumberOfPages = (int) Math.ceil((double) totalNumberOfProperties / (double) 60);
        } catch (NoSuchElementException e) {
            totalNumberOfPages = 1;
        }
        log.debug("{}: Found {} pages to go through.", driverWrapper.getName(), totalNumberOfPages);
        driverWrapper.setAvailable(true);

        // get all pages we need to scrape
        List<String> allPageLinks = new ArrayList<>();
        for (int i = 1; i <= totalNumberOfPages; i++) {
            allPageLinks.add(cityURL + Constants.URL.PAGINATION + i);
        }

        // scrape page data
        scraperDTO.setScraperResultDTOS(new ArrayList<>());
        for (int i = 0; i < allPageLinks.size(); i++) {
            String pageLink = allPageLinks.get(i);
            scrapePageData(driverWrapper, pageLink, i, totalNumberOfPages, scraperDTO);
        }
    }

    private static void scrapePageData(DriverWrapper driverWrapper, String pageLink, int index, int numberOfPages, ScraperDTO scraperDTO) {
        log.trace("{}: Navigating to {}.", driverWrapper.getName(), pageLink);
        WebDriver wd = driverWrapper.getWd();
        wd.get(pageLink);
        customWait(500);
        waitUntilElementFound(wd, By.className("property-list"));
        List<WebElement> properties = wd.findElements(By.className("property"));
        if (properties.isEmpty()) {

            if (consecutiveFails > 3) {
                consecutiveFails = 0;
                return;
            } else {
                consecutiveFails++;
            }

            log.debug("{}: Found 0 properties, re-scraping page data attempt {}.", driverWrapper.getName(), consecutiveFails + 1);
            scrapePageData(driverWrapper, pageLink, index, numberOfPages, scraperDTO);
            return;
        }
        log.debug("{}: Found {} properties on page {}/{}: {}.", driverWrapper.getName(), properties.size(), index + 1, numberOfPages, pageLink);
        for (WebElement property : properties) {
            WebElement info = property.findElement(By.className("info"));
            String title = info.findElement(By.className("title")).getText().replaceAll(" ", " "); // NOSONAR
            Integer squareMeters = getSquareMeters(title);
            String address = info.findElement(By.className("locality")).getText().replaceAll(" ", " "); // NOSONAR
            Integer price = getPrice(info.findElement(By.className("price")).getText());
            Integer pricePerSquareMeter = squareMeters == null || squareMeters == 0 || price == null ? null : price / squareMeters;
            String link = info.findElement(By.cssSelector("a[class='title']")).getAttribute("href");
            if (price != null && pricePerSquareMeter != null) {
                ScraperResultDTO scraperResultDTO = new ScraperResultDTO();
                scraperResultDTO.setScraperId(scraperDTO.getId());
                scraperResultDTO.setTitle(title);
                scraperResultDTO.setAddress(address);
                scraperResultDTO.setPrice(price);
                scraperResultDTO.setPricePerSquareMeter(pricePerSquareMeter);
                scraperResultDTO.setLink(link);
                scraperResultDTO.setCreated(getCurrentTimestamp());
                scraperResultDTO.setSquareMeters(squareMeters);

                scraperDTO.getScraperResultDTOS().add(scraperResultDTO);
            }
        }
        driverWrapper.setAvailable(true);
    }

    public static void scrapePropertyParams(DriverWrapper driverWrapper, ScraperResultDTO scraperResultDTO) {
        try {
            // extra params on property detail
            WebDriver wd = driverWrapper.getWd();
            log.trace("Scraping property params from: {}.", scraperResultDTO.getLink());
            wd.get(scraperResultDTO.getLink());
            customWait(500);
            WebElement propertyTitleElement = waitUntilElementFound(wd, By.cssSelector("div[class='property-title']"));
            if (propertyTitleElement != null) {
                WebElement params1 = waitUntilElementFound(wd, By.className("params1"));
                WebElement params2 = waitUntilElementFound(wd, By.className("params2"));

                List<ParameterDTO> parameterDTOS = new ArrayList<>();
                if (params1 != null) {
                    parameterDTOS.addAll(scrapeParams(params1, scraperResultDTO));
                }
                if (params2 != null) {
                    parameterDTOS.addAll(scrapeParams(params2, scraperResultDTO));
                }
                scraperResultDTO.setParameterDTOS(parameterDTOS);
            } else {
                log.debug("Timeout waiting for property params page to load, skipping. {}", scraperResultDTO.getLink());
            }
        } catch (IllegalStateException e) {
            if (NO_OFFER_FOUND_EXCEPTION.equals(e.getMessage())) {
                log.debug("Setting offer to unavailable. {}", scraperResultDTO.getLink());
                scraperResultDTO.setAvailable(false);
            } else {
                throw new IllegalStateException(e);
            }
        } catch (Exception e) { // NOSONAR
            // ignore, continue further
            log.debug("Exception (skipping) scraping property params from link: {}.", scraperResultDTO.getLink());
        }
        log.trace("Finished scraping property params from link: {}.", scraperResultDTO.getLink());
        driverWrapper.setAvailable(true);
    }

    private static List<ParameterDTO> scrapeParams(WebElement params, ScraperResultDTO scraperResultDTO) {
        List<ParameterDTO> result = new ArrayList<>();
        List<WebElement> paramParents = params.findElements(By.cssSelector("li"));
        for (WebElement paramParent : paramParents) {
            String key = paramParent.findElement(By.className("param-label")).getText();
            if (key.contains(":")) {
                key = key.replace(":", "");
            }
            WebElement strong = paramParent.findElement(By.cssSelector("strong"));
            String value = strong.getText();
            if (isEmpty(value)) {
                // most likely boolean value
                try {
                    WebElement icon = strong.findElement(By.className("icof"));
                    String booleanAttribute = icon.getAttribute("ng-if");
                    if ("item.type == 'boolean-false'".equals(booleanAttribute)) {
                        value = "false";
                    } else if ("item.type == 'boolean-true'".equals(booleanAttribute)) {
                        value = "true";
                    } else {
                        log.error("Expected a boolean attribute, but was something else: '{}'", booleanAttribute);
                    }
                } catch (Exception e) {
                    // ignore, can happen when there's really a missing value in the column
                }
            }
            ParameterDTO parameterDTO = new ParameterDTO();
            parameterDTO.setKey(key);
            parameterDTO.setValue(value);
            parameterDTO.setScraperResultId(scraperResultDTO.getId());
            result.add(parameterDTO);
        }
        return result;
    }

    /**
     * Attempts to parse the square meters value from the title of the property. Has to be able to handle different types of whitespaces.
     *
     * @param title e.g. Prodej bytu 2+1 70 m²
     * @return parsed square meters value or null if none found
     */
    public static Integer getSquareMeters(String title) {
        title = optionalSplit(title, "+kk", "+1");

        if (title.contains("/")) {
            title = title.substring(title.indexOf("/") + 4);
        }

        if (title.contains(",")) {
            String[] split = title.split(",");
            int total = 0;
            for (String s : split) {
                Integer squareMeters = getSquareMeters(s);
                if (squareMeters != null) {
                    total += squareMeters;
                }
            }
            return total;
        }

        String squareMeters = title.replaceAll("\\D", "");
        if (!StringUtils.isNullOrEmpty(squareMeters)) {
            try {
                return Integer.parseInt(squareMeters);
            } catch (NumberFormatException e) {
                log.error("Failed to parse square meters from title: {}.", title);
            }
        }
        return null;
    }

    private static String optionalSplit(String title, String... splitStrings) {
        for (String splitString : splitStrings) {
            if (title.contains(splitString)) {
                splitString = splitString.replaceAll("[+]", "[+]");
                title = title.split(splitString)[1];
            }
        }
        return title;
    }

    /**
     * Attempts to parse the price from the string.
     *
     * @param price e. g. 2 750 000 Kč
     * @return the parsed price from the string or null in case anything fails
     */
    public static Integer getPrice(String price) {
        Integer result = null;
        try {
            String tmp = price;
            if (tmp.contains("Kč")) {
                tmp = tmp.replaceAll(" ", ""); // NOSONAR
                tmp = tmp.substring(0, tmp.indexOf("Kč"));
                result = Integer.parseInt(tmp);
            }
        } catch (Exception e) {
            log.error("Failed to parse the price: {}.", price);
        }
        return result;
    }

    static int consecutiveFails = 0;

    public static WebElement waitUntilElementFound(WebDriver wd, By by) { // NOSONAR
        log.trace("Waiting for element found by: '{}', to be present and to contain any text.", by);
        AtomicBoolean noOfferFound = new AtomicBoolean(false);
        CompletableFuture<WebElement> future = CompletableFuture.supplyAsync(() -> {
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
                            noOfferFound.set(true);
                            throw new IllegalStateException(NO_OFFER_FOUND_EXCEPTION); // to bypass the timeout
                        }
                    } catch (NoSuchElementException ignore) {
                        // ignore not found exception
                    }
                    log.trace("Element {} not found, waiting.", by);
                    customWait(500);

                    if (consecutiveFails++ > 10) {
                        consecutiveFails = 0;
                        throw new IllegalStateException("Too many fails in a row, throwing an exception.");
                    }
                }
                customWait(100);
            }
        });
        try {
            return future.get(10, TimeUnit.SECONDS);
        } catch (Exception e) { // NOSONAR
            // ignore
            log.debug("Timeout waiting for element: {}", by);
        }
        if (noOfferFound.get()) {
            throw new IllegalStateException(NO_OFFER_FOUND_EXCEPTION);
        }
        return null;
    }

    /**
     * Exception wrapper for Thread#sleep.
     *
     * @param timeInMillis wait time in milliseconds
     */
    public static void customWait(long timeInMillis) {
        try {
            Thread.sleep(timeInMillis);
        } catch (InterruptedException e) { // NOSONAR
            // ignore
        }
    }

    /**
     * Adjusts the result size from 20 to 60 per page. Persists through the whole scraping. It's possible that this value is stored in another cookie,
     * so there could be a workaround and a potential perf improvement for this.
     */
    public static void increaseResultSize(WebDriver wd, String searchValue) {
        log.trace("Increasing result size.");

        // navigate to a site with results
        wd.get(Constants.URL.BASE_SEARCH_URL.replace("${searchValue}", searchValue) + Constants.CITY.PRIBRAM);
        customWait(500);
        waitUntilElementFound(wd, By.className("property-list"));

        // increase the result size
        wd.findElement(By.className("per-page")).findElement(By.cssSelector("span[list-select='listSelectConf'")).click();
        WebElement perPageSelect = waitUntilElementFound(wd, By.className("per-page-select"));
        assert perPageSelect != null;
        perPageSelect.findElement(By.className("options")).findElement(By.cssSelector("li:last-of-type")).findElement(By.cssSelector("button")).click();

        log.trace("Done increasing result size.");
    }

    /**
     * Workaround for GDPR consent via a cookie. Goes to a non-existing website on the domain which we need to add the cookie for
     * avoiding ugly redirect to <a href="https://www.seznam.cz">seznam.cz</a> where the actual consent form is located.
     */
    public static void dealWithCookies(WebDriver wd) {
        log.trace("Dealing with cookies.");
        Cookie consentCookie = Util.getCookie(Constants.Cookie.CONSENT);
        log.trace("Navigating to domain.");
        wd.get(Constants.URL.BASE_SETUP_URL + "/404");
        customWait(2000);
        wd.get(Constants.URL.BASE_SETUP_URL + "/404");
        waitUntilElementFound(wd, By.className("error-description"));
        wd.manage().deleteAllCookies();
        log.trace("Adding cookie.");
        wd.manage().addCookie(consentCookie);
        log.trace("Done dealing with cookies.");
    }

    /**
     * Creates a {@link Cookie} from a string representation of a json.
     *
     * @param json being parsed as a {@link Cookie}
     * @return {@link Cookie} from a string representation of a json
     */
    public static Cookie getCookie(String json) {
        return new Gson().fromJson(json, Cookie.class);
    }

    /**
     * Creates and saves the Excel file containing all the property values. Excel headers are created from the object field names
     * and property values are taken from the field values themselves.
     * <p>
     * This can be made more abstract in the future. No need to do so, for now.
     *
     * @param properties as data being written into the Excel file
     * @param fileName   of the output file
     * @return Excel file containing all the property values
     */
    @SuppressWarnings("UnusedReturnValue") // future plans
    public static File exportResults(List<Property> properties, String fileName) {
        File file = new File(Constants.RESULTS_FOLDER);
        if (!file.exists() || !file.isDirectory()) {
            boolean mkdir = file.mkdir();
            if (!mkdir) {
                throw new IllegalStateException("Failed to create the results directory.");
            }
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd");
        String date = "_" + sdf.format(new Date());
        String[] split = fileName.split("/");
        fileName = split[split.length - 1] + date + ".xlsx";
        fileName = Constants.RESULTS_FOLDER + File.separator + fileName;
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("sreality.cz properties");

            // create the header
            int rowIndex = 0;
            Row headerRow = sheet.createRow(rowIndex++);
            int cellIndex = 0;
            Field[] headers = Property.class.getDeclaredFields();
            for (Field declaredField : headers) {
                Cell cell = headerRow.createCell(cellIndex++);
                cell.setCellValue(declaredField.getName());
            }

            // write the rest of the values
            for (Property property : properties) {
                Row row = sheet.createRow(rowIndex++);
                cellIndex = 0;
                for (Field declaredField : Property.class.getDeclaredFields()) {
                    declaredField.setAccessible(true); // NOSONAR
                    Cell cell = row.createCell(cellIndex++);
                    if (declaredField.getType().isAssignableFrom(Integer.class)) {
                        cell.setCellValue((int) declaredField.get(property));
                    } else {
                        cell.setCellValue(declaredField.get(property).toString());
                    }
                }
            }

            // autosize columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            FileOutputStream fos;
            fos = new FileOutputStream(fileName);
            wb.write(fos);
            fos.close();
            LOGGER.debug("File saved as: {}.", fileName);
            return new File(fileName);
        } catch (Exception e) {
            LOGGER.error("Exception", e);
        }
        return null;
    }

    public static Timestamp getCurrentTimestamp() {
        return new Timestamp(new Date().getTime());
    }
}
