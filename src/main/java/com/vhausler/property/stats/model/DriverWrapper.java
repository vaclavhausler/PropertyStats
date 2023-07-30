package com.vhausler.property.stats.model;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.NoSuchSessionException;
import org.openqa.selenium.WebDriver;

import java.time.Duration;
import java.time.Instant;

@Data
@Slf4j
public class DriverWrapper {

    private static int nameIndex = 0;
    private static String[] names = new String[]{
            "Alpha", "Beta", "Gamma", "Delta", "Epsilon", "Zeta", "Eta", "Theta", "Iota", "Kappa", "Lambda", "Mu", "Nu", "Xi", "Omicron", "Pi", "Rho", "Sigma", "Tau", "Upsilon", "Phi", "Chi", "Psi", "Omega"
    };
    private String name;
    private final WebDriver wd;
    private boolean available;
    private Instant created;

    public DriverWrapper(WebDriver wd) {
        this.wd = wd;
        available = true;
        created = Instant.now();
        name = names[nameIndex++];
        if (nameIndex > names.length - 1) {
            nameIndex = 0;
        }
    }

    public void quit() {
        log.debug("{}: attempting to gracefully quit.", name);
        try {
            wd.close();
            wd.quit();
        } catch (NoSuchSessionException ignore) {
            // ignore
        } catch (Exception e) {
            log.error("{}: quitting failed.", name, e);
        }
    }

    public boolean isOutdated(long timeout) {
        return Duration.between(created, Instant.now()).getSeconds() > timeout;
    }
}