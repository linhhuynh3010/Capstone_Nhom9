package pages;

import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.*;
import org.testng.Reporter;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.regex.*;

public class ProfilePage extends BasePage {
    public ProfilePage(WebDriver p) {
        super(p);
    }
    private final By lastServiceInList = By.xpath("(//a[contains(@href, '/jobDetail/')])[last()]");
    private final By serviceXpathGeneral = By.xpath("//div[@class='gigs_card']");

    public boolean verifyLastBookedService(String expectedServiceID) throws InterruptedException {
        goToProfilePage();
        scrollToShowLastBookedService();
        String actualServiceID = getJobIdFromUrl(getElementHref());

        Reporter.log("Expected Service ID: " + expectedServiceID, true);
        Reporter.log("Actual Service ID: " + actualServiceID, true);

        return Objects.equals(expectedServiceID, actualServiceID);
    }

    public void goToProfilePage() throws InterruptedException {
        driver.get("https://demo5.cybersoft.edu.vn/profile");
        new WebDriverWait(driver, Duration.ofSeconds(5)).until(ExpectedConditions.presenceOfElementLocated(lastServiceInList));
    }

    public String getElementHref() {
        WebElement element = driver.findElement(lastServiceInList);
        return element.getAttribute("href");
    }

    public void scrollToShowLastBookedService() throws InterruptedException {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].scrollIntoView(true);", driver.findElement(lastServiceInList));
        Thread.sleep(1000);
    }

    public String getJobIdFromUrl(String url) {
        if (url == null || url.isBlank()) return "";
        return url.replaceAll(".*/", ""); // lấy phần sau dấu /
    }

    public int countServices() {
        List<WebElement> services = driver.findElements(serviceXpathGeneral);
        return services.size();
    }

    public boolean verifyNumberOfBookedService(int expectedNumberOfService) throws InterruptedException {
        goToProfilePage();
        int actualNumberOfService = countServices();

        Reporter.log("Expected Number of Service: " + expectedNumberOfService, true);
        Reporter.log("Actual Number of Service: " + actualNumberOfService, true);

        return expectedNumberOfService == actualNumberOfService;
    }
}
