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
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS");

    /**
     * Dùng để nhúng trực tiếp vào ExtentReport (khuyến nghị)
     */
    public static String captureBase64(WebDriver driver) {
        try {
            return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BASE64);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Lưu file vào reports/screenshots/ và trả về đường dẫn TƯƠNG ĐỐI từ file report (screenshots/xxx.png)
     */
    public static String captureToFile(WebDriver driver, String testName) {
        try {
            String safe = (testName == null ? "screenshot" : testName).replaceAll("[^a-zA-Z0-9-_\\.]", "_");
            String fileName = safe + "_" + LocalDateTime.now().format(TS) + ".png";

            Path reportsDir = Paths.get("reports");
            Path shotsDir = reportsDir.resolve("screenshots");
            Files.createDirectories(shotsDir);

            Path png = shotsDir.resolve(fileName);
            byte[] bytes = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            Files.write(png, bytes);

            // Trả về đường dẫn tương đối: "screenshots/xxx.png"
            String rel = reportsDir.relativize(png).toString().replace('\\', '/');
            return rel;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Giữ tương thích với code cũ (nếu nơi khác còn gọi)
     */
    public static String capture(WebDriver driver, String testName) {
        return captureToFile(driver, testName);
    }
}
