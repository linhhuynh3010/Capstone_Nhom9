package pages;

import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class BasePage {
    protected final WebDriver driver;
    protected final WebDriverWait wait;

    // có thể để public static nếu muốn dùng từ ngoài
    protected static final By OVERLAYS = By.cssSelector(
            ".modal-backdrop, .swal2-container, .ant-message, .Toastify__toast, " +
                    ".loading, .spinner, [data-state='open'], .ant-modal-wrap, .ant-drawer-open"
    );

    protected BasePage(WebDriver d) {
        this.driver = d;
        this.wait = new WebDriverWait(d, Duration.ofSeconds(12));
    }

    public void waitPageReady() {
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(dr -> "complete".equals(((JavascriptExecutor) dr)
                        .executeScript("return document.readyState")));
        waitOverlaysGone();
    }

    protected void waitOverlaysGone() {
        new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(d -> d.findElements(OVERLAYS).stream().noneMatch(WebElement::isDisplayed));
    }

    protected void scrollCenter(WebElement el) {
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block:'center'});", el);
        ((JavascriptExecutor) driver).executeScript(
                "window.scrollBy(0, -Math.min(120, Math.floor(window.innerHeight/8)));");
    }

    protected void safeClick(WebElement el) {
        scrollCenter(el);
        WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(6));
        try {
            shortWait.until(ExpectedConditions.elementToBeClickable(el)).click();
        } catch (TimeoutException | ElementClickInterceptedException e) {
            if (isVisible(el)) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
            } else {
                try {
                    new Actions(driver).moveToElement(el).perform();
                } catch (Exception ignore) {
                }
                scrollCenter(el);
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
            }
        }
    }

    protected boolean isVisible(WebElement el) {
        try {
            return el.isDisplayed() && !"true".equalsIgnoreCase(el.getAttribute("aria-hidden"));
        } catch (StaleElementReferenceException e) {
            return false;
        }
    }

    protected long getClientHeight(WebElement el) {
        Object h = ((JavascriptExecutor) driver).executeScript("return arguments[0].clientHeight||0;", el);
        return (h instanceof Number) ? ((Number) h).longValue() : 0L;
    }

    protected String getInnerText(WebElement el) {
        Object t = ((JavascriptExecutor) driver)
                .executeScript("return arguments[0].innerText || arguments[0].textContent || '';", el);
        return String.valueOf(t);
    }

    protected void refresh() {
        try {
            driver.navigate().refresh();
        } catch (Exception ignore) {
        }
    }

    //Chuyển tới trang/khung login?
    public boolean waitForLoginRedirect() {
        try {
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.urlMatches(".*/(login|signin)(\\?.*)?$")
            ));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

}



