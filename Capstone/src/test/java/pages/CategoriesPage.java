package pages;

/*
 * @Author: Huỳnh Mai Linh
 * @Version: 1.0
 * @Function: Page Categories
 * */
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.List;
import java.util.regex.*;


public class CategoriesPage extends BasePage {

    public CategoriesPage(WebDriver d) {
        super(d);
    }

    private final By servicesAvailable = By.xpath("//div[@class='number-of-categories']");
    private final By jobCards = By.xpath("//a[contains(@href,'/jobDetail/')]/ancestor::*[self::article or self::div][1]");
    private final By sortBy = By.xpath("//div[contains(@class,'sort-by')]//select");
    private final By page2 = By.xpath("//a[normalize-space()='2' or @data-page='2' or contains(@href,'page=2')]");
    private final By HEART = By.cssSelector(".service-card .heart-icon, .card .heart-icon");
    // selector tổng hợp cho ARIA modal
    private final By MODAL_OR_DIALOG = By.cssSelector(
            "[role='dialog'][aria-modal='true'], dialog[open], .modal.show, .ant-modal-wrap, " + ".MuiModal-root.MuiModal-open, .chakra-modal__content-container, .mantine-Modal-root");

    // Popover/dropdown phổ biến
    private final By POPOVER_OR_DROPDOWN = By.cssSelector(".popover.show, .dropdown-menu.show, [data-testid*='popover' i], .ant-popover-open, .MuiPopover-root");

    // Toast/snackbar phổ biến
    private final By TOAST_OR_SNACK = By.cssSelector(".toast.show, .ant-message, .MuiSnackbar-root.MuiSnackbar-anchorOriginTopCenter, [role='status']");

    // wrapper của từng dropdown filter
    private By dropdownWrapperByLabel(String label) {
        return By.xpath("//div[contains(@class,'categories-topbar-dropdown-filter')]" + "[.//button[contains(@class,'dropdown-toggle') and contains(normalize-space(.),'" + label + "')]]");
    }


    // ===== ACTION =====

    private boolean isHeartActive(WebElement heart) {
        try {
            if (!heart.isDisplayed()) return false;
            String ariaPressed = String.valueOf(heart.getAttribute("aria-pressed"));
            String ariaChecked = String.valueOf(heart.getAttribute("aria-checked"));
            String dataState = String.valueOf(heart.getAttribute("data-state"));
            String cls = String.valueOf(heart.getAttribute("class"));
            return "true".equalsIgnoreCase(ariaPressed)
                    || "true".equalsIgnoreCase(ariaChecked)
                    || "on".equalsIgnoreCase(dataState)
                    || cls.matches(".*(active|selected|favorited|is-active).*");
        } catch (StaleElementReferenceException e) {
            return false;
        }
    }


    public int getServicesAvailable() {
        String txt = wait.until(ExpectedConditions.visibilityOfElementLocated(servicesAvailable)).getText();
        Matcher m = Pattern.compile("(\\d+)").matcher(txt);
        return m.find() ? Integer.parseInt(m.group(1)) : -1;
    }

    //Mở dropdown theo label và chọn option hợp lệ đầu tiên, trả về text option đã chọn

    public String openDropdownAndChooseFirst(String label) {
        WebElement wrapper = wait.until(ExpectedConditions.presenceOfElementLocated(dropdownWrapperByLabel(label)));

        WebElement button = wrapper.findElement(By.xpath(".//button[contains(@class,'dropdown-toggle')]"));

        // mở dropdown + chờ thật sự mở
        safeClick(button);
        wait.until(ExpectedConditions.attributeToBe(button, "aria-expanded", "true"));
        WebElement menu = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath(".//ul[contains(@class,'dropdown-menu') and contains(@class,'show')]")));

        // chỉ lấy item hợp lệ (anchor), bỏ header "All Categories"
        List<WebElement> items = menu.findElements(
                By.xpath(".//a[contains(@class,'dropdown-item') and not(contains(@class,'disabled'))]"));
        if (items.isEmpty()) throw new NoSuchElementException("Không tìm thấy option trong dropdown: " + label);

        WebElement first = items.get(1);
        String chosenText = first.getText().trim();

        safeClick(first);

        // chờ menu đóng (nếu có)
        try {
            wait.until(ExpectedConditions.invisibilityOf(menu));
        } catch (Exception ignored) {
        }

        return chosenText;
    }

    // Đếm số card hiển thị (đa chiến lược cho linh hoạt UI)
    public int getVisibleCardCount() {
        List<WebElement> els = new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.presenceOfAllElementsLocatedBy(jobCards));
        int count = 0;
        for (WebElement e : els) if (e.isDisplayed()) count++;
        return count;
    }

    // (A) Bật/tắt Pro services qua hàm có sẵn toggleByLabel
    public void toggleProServices() {
        toggleByLabel("Pro services");
    }


    //Đếm số card đang gắn nhãn Pro (dò badge/text bên trong mỗi card).
    public int countProCards() {
        int count = 0;
        var cards = driver.findElements(jobCards);
        for (WebElement c : cards) {
            boolean isPro = !c.findElements(By.xpath(
                    ".//*[contains(@class,'pro') or contains(.,'Pro')][not(self::script)]"
            )).isEmpty();
            if (isPro) count++;
        }
        return count;
    }


    // Đợi danh sách card reload (số lượng thay đổi hoặc có card hiển thị)

    public void waitCardsReloaded(int previousCount) {
        new WebDriverWait(driver, Duration.ofSeconds(10)).until(d -> {
            int now = getCardCount();
            return now != previousCount || now > 0;
        });
    }

    public int getCardCount() {
        return driver.findElements(jobCards).size();
    }


    // Bật/Tắt Local sellers
    public void toggleLocalSellers() {
        toggleByLabel("Local sellers");
    }

    // Bật/Tắt Online sellers
    public void toggleOnlineSellers() {
        toggleByLabel("Online sellers");
    }

    // Lấy text sort hiện tại (hỗ trợ cả <select> và button/div)
    public String getSortText() {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(sortBy));
        if ("select".equalsIgnoreCase(el.getTagName())) {
            return new Select(el).getFirstSelectedOption().getText().trim();
        }
        return el.getText().trim();
    }

    // Chọn sort theo nhãn hiển thị (Best Selling / New Arrivals / Relevance)
    public void selectSortBy(String label) {
        WebElement el = wait.until(ExpectedConditions.elementToBeClickable(sortBy));
        el.click();
        if ("select".equalsIgnoreCase(el.getTagName())) {
            new Select(el).selectByVisibleText(label);
            return;
        }
        // menu dạng dropdown: chọn item theo text
        By option = By.xpath(
                "((//*[@role='listbox' or @role='menu' or contains(@class,'menu') or contains(@class,'options')])[1]"
                        + "//*[self::a or self::button or self::div or self::span][normalize-space()='" + label + "'])[1]"
        );
        wait.until(ExpectedConditions.elementToBeClickable(option)).click();
    }

    // Đợi text sort thay đổi (xác nhận đã đổi thành công)
    public void waitSortChangedFrom(String previousText) {
        new WebDriverWait(driver, Duration.ofSeconds(10)).until(d -> {
            try {
                String curr = getSortText();
                return curr != null && !curr.equalsIgnoreCase(previousText);
            } catch (Exception e) {
                return false;
            }
        });
    }


    //Click ♥ của card đầu tiên và trả về chính element đó để theo dõi trạng thái
    public WebElement clickFirstHeart() {
        wait.until(ExpectedConditions.or(
                ExpectedConditions.visibilityOfElementLocated(HEART),
                ExpectedConditions.presenceOfElementLocated(HEART)
        ));
        WebElement heart = driver.findElements(HEART).get(0);
        new Actions(driver).moveToElement(heart).perform();
        safeClick(heart);
        return heart;
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

    // Mở modal
    public boolean waitForFavoriteFeedback(WebElement heart) {
        return wait.until(d -> {
            // 1) Heart đổi trạng thái (thường có aria-pressed/checked hoặc class active)
            if (isHeartActive(heart)) return true;

            // 2) Có modal/dialog thực sự hiện
            for (WebElement e : d.findElements(MODAL_OR_DIALOG))
                if (isVisible(e)) return true;

            // 3) Hoặc popover/dropdown “Add to list/Collection”
            for (WebElement e : d.findElements(POPOVER_OR_DROPDOWN))
                if (isVisible(e)) return true;

            // 4) Hoặc hiện toast/snackbar
            for (WebElement e : d.findElements(TOAST_OR_SNACK))
                if (isVisible(e)) return true;

            return false;
        });
    }


    public void toggleByLabel(String label) {
        By tg = By.xpath("//*[contains(.,'" + label + "')]/preceding::*[self::input[@type='checkbox'] or self::button[contains(@class,'toggle')]][1] | //*[contains(.,'" + label + "')]/following::*[self::input[@type='checkbox'] or self::button[contains(@class,'toggle')]][1]");
        wait.until(ExpectedConditions.elementToBeClickable(tg)).click();
    }


    public boolean goToPage2IfExists() {
        var els = driver.findElements(page2);
        if (els.isEmpty()) return false;
        els.get(0).click();
        return true;
    }

    //
    public List<WebElement> findAll(By locator) {
        return driver.findElements(locator);
    }

    public int getJobCardCount() {
        return findAll(jobCards).size();
    }

    public void clickFirstCard(int index) {
        List<WebElement> cards = findAll(jobCards);
        if (cards.isEmpty()) throw new NoSuchElementException("Không tìm thấy job card");
        if (index < 0 || index >= cards.size()) index = 0;
        safeClick(cards.get(index));
    }

}
