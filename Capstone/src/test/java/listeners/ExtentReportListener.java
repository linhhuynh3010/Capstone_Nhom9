package listeners;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import org.openqa.selenium.WebDriver;
import org.testng.*;

import utils.ExtentManager;
import utils.ScreenshotUtil;

import java.lang.reflect.Field;

public class ExtentReportListener implements ITestListener, ISuiteListener {
    private static ExtentReports extent;
    private static ThreadLocal<ExtentTest> test = new ThreadLocal<>();

    @Override
    public void onStart(ISuite suite) {
        extent = ExtentManager.getInstance();
    }

    @Override
    public void onFinish(ISuite suite) {
        extent.flush();
    }

    @Override
    public void onTestStart(ITestResult result) {
        test.set(extent.createTest(result.getMethod().getMethodName()));
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        test.get().pass("Passed");
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        test.get().skip("Skipped");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        test.get().fail(result.getThrowable());
        WebDriver driver = extractDriver(result);
        if (driver != null) {
            String path = ScreenshotUtil.capture(driver, result.getMethod().getMethodName());
            if (path != null) {
                try {
                    test.get().fail("Screenshot:", MediaEntityBuilder.createScreenCaptureFromPath(path).build());
                } catch (Exception ignored) {
                }
            }
        }
    }

    private WebDriver extractDriver(ITestResult result) {
        Object instance = result.getInstance();
        if (instance == null) return null;
        Class<?> c = instance.getClass();
        while (c != null) {
            try {
                var f = c.getDeclaredField("driver");
                f.setAccessible(true);
                Object v = f.get(instance);
                if (v instanceof WebDriver) return (WebDriver) v;
            } catch (Exception e) {
            }
            c = c.getSuperclass();
        }
        return null;
    }
}
