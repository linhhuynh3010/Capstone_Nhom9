package utils;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ScreenshotUtil {
    public static String capture(WebDriver driver, String testName) {
        try {
            String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS"));
            String rel = "reports/screenshots/" + testName + "_" + time + ".png";
            Path p = Paths.get(rel);
            Files.createDirectories(p.getParent());
            byte[] bytes = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            Files.write(p, bytes);
            return rel;
        } catch (Exception e) {
            return null;
        }
    }
}
