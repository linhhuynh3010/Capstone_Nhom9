package scripts;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import pages.JobDetailPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pages.ProfilePage;

import java.time.Duration;

public class BookingFlowTest extends BaseTest {
    @Test(groups = {"WithLogin"}, description = "Verify that user can book service successfully when logged in with message \"Thuê công việc thành công\"")
    public void BF_F01_VerifyBookingServiceSuccessfully() {
        go(pathDetail());
        JobDetailPage p = new JobDetailPage(driver);
        p.waitPageReady();

        Assert.assertTrue(
                p.continueAndWaitHireSuccess(Duration.ofSeconds(10)),
                "Không thấy message 'Thuê công việc thành công'. URL: " + driver.getCurrentUrl()
        );
    }

    @Test(groups = {"WithLogin"}, description = "Verify that user can see message \"Bạn có chắc muốn thuê lại dịch vụ này?\" when booking a service twice.")
    public void BF_F05_VerifyBookingAServiceTwice() {
        go(pathDetail());
        JobDetailPage p = new JobDetailPage(driver);
        p.waitPageReady();

        Assert.assertTrue(
                p.continueAndWaitHireSuccess(Duration.ofSeconds(10)),
                "Không thấy message 'Thuê công việc thành công'. URL: " + driver.getCurrentUrl()
        );

        Assert.assertTrue(
                p.continueAndWaitRehireConfirm(Duration.ofSeconds(10)),
                "Không thấy message 'Bạn có chắc muốn thuê lại dịch vụ này?'. URL: " + driver.getCurrentUrl()
        );
    }

    @Test(groups = {"WithLogin"}, description = "Verify that user can see booked service at the end of profile page.")
    public void BF003_VerifyBookedServiceInProfilePage() throws InterruptedException {
        go(pathDetail());
        JobDetailPage p = new JobDetailPage(driver);
        p.waitPageReady();
        String serviceID = p.getJobIdFromUrl();

        Assert.assertTrue(
                p.continueAndWaitHireSuccess(Duration.ofSeconds(10)),
                "Không thấy message 'Thuê công việc thành công'. URL: " + driver.getCurrentUrl()
        );

        ProfilePage pp = new ProfilePage(driver);
        pp.goToProfilePage();
        Assert.assertTrue(
                pp.verifyLastBookedService(serviceID),
                "Newest service is not true: expected=" + serviceID + driver.getCurrentUrl()
        );
    }

    @Test(groups = {"WithLogin"}, description = "Verify that number of service increased 1 after booking a service successfully.")
    public void BF004_VerifyBookedServiceInProfilePage() throws InterruptedException {
        ProfilePage pp = new ProfilePage(driver);
        pp.goToProfilePage();
        int expectedNumber = pp.countServices() + 1;

        go(pathDetail());
        JobDetailPage p = new JobDetailPage(driver);
        p.waitPageReady();

        Assert.assertTrue(
                p.continueAndWaitHireSuccess(Duration.ofSeconds(10)),
                "Không thấy message 'Thuê công việc thành công'. URL: " + driver.getCurrentUrl()
        );
        Assert.assertTrue(
                pp.verifyNumberOfBookedService(expectedNumber),
                "Number of booked service is not increased: expected=" + expectedNumber + driver.getCurrentUrl()
        );
    }

    @Test(groups = {"WithLogin"}, description = "Verify that user can book a service after minimizing the window.")
    public void BF_F10_VerifyBookingServiceSuccessfullyInMinimizedWindow() throws InterruptedException {
        go(pathDetail());
        JobDetailPage p = new JobDetailPage(driver);
        p.waitPageReady();
        p.minimizeWindow();

        Assert.assertTrue(
                p.continueAndScrollToContinueButtonAndWaitHireSuccess(Duration.ofSeconds(10)),
                "Không thấy message 'Thuê công việc thành công'. URL: " + driver.getCurrentUrl()
        );
    }

    @Test(groups = {"WithoutLogin"}, description = "Verify that user can NOT book service successfully when NOT logged in with message \"Mời đăng nhập trước khi thuê công việc\"")
    public void BF005_VerifyBookingServiceUnsuccessfully() {
        go(pathDetail());
        JobDetailPage p = new JobDetailPage(driver);
        p.waitPageReady();
        p.clickContinue();
        Assert.assertTrue(
                p.continueAndWaitHireSuccess(Duration.ofSeconds(10)),
                "Không thấy message 'Mời đăng nhập trước khi thuê công việc'. URL: " + driver.getCurrentUrl()
        );
    }
}
