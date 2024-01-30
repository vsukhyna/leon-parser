package ua.sukhyna;

import org.openqa.selenium.WebDriver;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static ua.sukhyna.configuration.WebDriverConfig.getWebDriver;

public class Main {

    private static final List<String> SPORTS = List.of("soccer", "tennis", "hockey", "basketball");

    public static void main(String[] args) {

        ExecutorService executorService = Executors.newFixedThreadPool(3);

        try {
            for (String sport : SPORTS) {
                executorService.submit(() -> {
                    WebDriver driver = getWebDriver();
                    try {
                        LeonParser leonParser = new LeonParser();
                        leonParser.getBettingBySport(sport, driver);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    } finally {
                        driver.quit();
                    }
                });
            }
        } finally {
            executorService.shutdown();
        }
    }
}