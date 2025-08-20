package listeners;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import org.openqa.selenium.WebDriver;
import org.testng.*;

import utils.ExtentManager;
import utils.ScreenshotUtil;

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
        test.get().skip(result.getThrowable() != null
                ? result.getThrowable()
                : new SkipException("Skipped"));
    }

    @Override
    public void onTestFailure(ITestResult result) {
        test.get().fail(result.getThrowable());

        WebDriver driver = extractDriver(result);
        if (driver != null) {
            // 1) Gắn Base64 để đảm bảo hiển thị
            String b64 = ScreenshotUtil.captureBase64(driver);
            if (b64 != null && !b64.isEmpty()) {
                try {
                    test.get().fail("Screenshot:",
                            MediaEntityBuilder.createScreenCaptureFromBase64String(b64).build());
                } catch (Exception ignored) {
                }
            }
            // 2) Lưu file và log đường dẫn tương đối (screenshots/...)
            String relPath = ScreenshotUtil.captureToFile(driver, result.getMethod().getMethodName());
            if (relPath != null) {
                test.get().info("Saved to: " + relPath);
                // Nếu muốn gắn theo path file thay vì Base64:
                // test.get().fail("Screenshot (by path):",
                //        MediaEntityBuilder.createScreenCaptureFromPath(relPath).build());
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
            } catch (Exception ignored) {
            }
            c = c.getSuperclass();
        }
        return null;
    }
}
