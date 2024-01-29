package ua.sukhyna.configuration;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.HashMap;
import java.util.Map;

public class WebDriverConfig {

    public static WebDriver initDriver() {

        WebDriverManager.chromedriver().setup();

        Map<String, Object> prefs = new HashMap<>();

        prefs.put("intl.accept_languages", "en-US"); //прибираємо автоматичних перехід на іншу мову

        ChromeOptions options = new ChromeOptions();
        options.setExperimentalOption("prefs", prefs);
        options.addArguments();

        options.addArguments(
                "--headless",
                "--disable-gpu",
                "--ignore-certificate-errors",
                "--disable-extensions",
                "--no-sandbox",
                "--window-size=1920x1080",
                "--disable-dev-shm-usage");

        return new ChromeDriver(options);
    }
}