package listeners;

import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class SimpleListener implements ITestListener {
    @Override
    public void onTestStart(ITestResult result) {
        System.out.println("[START] " + result.getName());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        System.out.println("[PASS] " + result.getName());
    }

    @Override
    public void onTestFailure(ITestResult result) {
        System.out.println("[FAIL] " + result.getName());
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        System.out.println("[SKIP] " + result.getName());
    }

    @Override
    public void onStart(ITestContext context) {
        System.out.println(">>> TestNG Start");
    }

    @Override
    public void onFinish(ITestContext context) {
        System.out.println(">>> TestNG Finish");
    }
}
