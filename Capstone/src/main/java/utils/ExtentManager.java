package utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;

import java.nio.file.Files;
import java.nio.file.Paths;

public class ExtentManager {
    private static ExtentReports extent;

    public synchronized static ExtentReports getInstance() {
        if (extent == null) {
            try {
                Files.createDirectories(Paths.get("reports"));
            } catch (Exception ignored) {
            }
            ExtentSparkReporter spark = new ExtentSparkReporter("reports/ExtentReport.html");
            extent = new ExtentReports();
            extent.attachReporter(spark);
            extent.setSystemInfo("Project", "Selenium Capstone");
            extent.setSystemInfo("Java", System.getProperty("java.version"));
        }
        return extent;
    }
}
