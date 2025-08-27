package pages;
/*
 * @Author: Huỳnh Mai Linh
 * @Version: 1.0
 * @Function: Page Title
 * */

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;



public class TitlePage extends BasePage {

    public TitlePage(WebDriver d) {
        super(d);
    }

    // Popular chips/tags

    private final By mostPopularChip = By.xpath("//section[@class='popular-job-title']//div[@class='content']//div[1]");

    // Explore cards

    private final By exploreFirstCard = By.xpath("(//section[contains(@class,'explore-job-title')]//a[contains(@href,'/categories')])[1]");

    // Hero video
    private final By howItWorksBtn = By.xpath("(//a|//button)[contains(normalize-space(),'How Fiverr Works')]");

    //breadcrumb
    private final By breadcrumbOrHeading = By.xpath("(//nav[contains(@aria-label,'breadcrumb')] | //*[(self::h1 or self::h2) and string-length(normalize-space())>0])[1]");

    //S Related
    private final By servicesRelatedTag = By.xpath("(//section[contains(@class,'related-job-title')]//div[contains(@class,'tags')]//*[self::a or self::span])[1]");
    private final By page2 = By.xpath("//a[normalize-space()='2' or @data-page='2' or contains(@href,'page=2')]");
    // Slide list
    private static final By SLIDE_ITEMS = By.cssSelector(
            "[data-testid='slide-item'], .slide-list .swiper-slide, .carousel .slide, .slick-slide"
    );


    // ===== Helpers (keep names close to common usage) =====
    public List<WebElement> findAll(By locator) {
        return driver.findElements(locator);
    }


    // ===== ACTION =====


    // Explore
    public int getExploreCardCount() {
        return findAll(exploreFirstCard).size();
    }

    public void clickExploreCard(int index) {
        List<WebElement> cards = findAll(exploreFirstCard);
        if (cards.isEmpty()) throw new NoSuchElementException("Không tìm thấy explore card");
        if (index < 0 || index >= cards.size()) index = 0;
        safeClick(cards.get(index));
    }

    // Video

    private WebElement findHowFiverrWorks() {
        List<WebElement> els = findAll(howItWorksBtn);
        if (!els.isEmpty()) return els.get(0);
        // text fallback (clickable thing with text)
        return driver.findElement(howItWorksBtn);
    }

    public boolean isVideoVisible() {
        if (!driver.findElements(By.tagName("video")).isEmpty()) return true;
        for (WebElement f : driver.findElements(By.tagName("iframe"))) {
            String src = String.valueOf(f.getAttribute("src"));
            if (src.contains("youtube.com") || src.contains("vimeo.com") || src.contains("player")) return true;
        }
        return false;
    }

    public void clickHowFiverrWorks() {
        waitPageReady();
        WebElement el = findHowFiverrWorks();
        scrollCenter(el);
        safeClick(el);
    }

    // Carousel
    private WebElement findFirstPopularChip() {
        List<WebElement> els = findAll(mostPopularChip);
        if (!els.isEmpty()) return els.get(0);
        // text fallback (clickable thing with text)
        return driver.findElement(mostPopularChip);
    }

    public void clickFirstPopularChip() {
        waitPageReady();
        WebDriverWait w = new WebDriverWait(driver, Duration.ofSeconds(12));
        w.until(ExpectedConditions.presenceOfElementLocated(mostPopularChip));
        WebElement el = findFirstPopularChip();
        scrollCenter(el);
        safeClick(el);
    }


    //breadcrumb
    public boolean hasBreadcrumbOrHeading() {
        return !driver.findElements(breadcrumbOrHeading).isEmpty();
    }

    public void clickFirstServicesRelatedTag() {
        waitPageReady();
        WebDriverWait w = new WebDriverWait(driver, Duration.ofSeconds(12));
        WebElement btn = w.until(ExpectedConditions.presenceOfElementLocated(servicesRelatedTag));
        scrollCenter(btn);
        safeClick(btn);
    }

    //pagination
    public boolean goToPage2IfExists() {
        var els = driver.findElements(page2);
        if (els.isEmpty()) return false;
        els.get(0).click();
        return true;
    }

}
