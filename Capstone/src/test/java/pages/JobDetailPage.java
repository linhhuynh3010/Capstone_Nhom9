package pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.List;
import java.util.regex.*;

public class JobDetailPage extends BasePage {
    public JobDetailPage(WebDriver d) {
        super(d);
    }

    private By packageTab(String name) {
        return By.xpath("((//*[self::button or self::a or self::li][normalize-space()='" + name + "']) | (//*[contains(@class,'package') and contains(.,'" + name + "')]))[1]");
    }

    // Panel đang active của package
    private final By ACTIVE_PANE = By.cssSelector(".tab-pane.active");

    // Tiêu đề trong panel (Basic/Standard/Premium)
    private final By PANE_TITLE = By.cssSelector(
            ".check-out-body .price .title, .price .title, .package-title, span.title"
    );

    protected final By TOAST_ANY = By.xpath("//div[@role='alert']");

    private final By continueBtn = By.xpath("//section[contains(@class,'job-detail')]//div[contains(@class,'check-out-footer')]//button[@type='button' and contains(@class,'submit')]");
    private final By commentBox = By.xpath("(//*[contains(.,'Leave some comments')]/following::textarea)[1]");
    private final By commentBtn = By.xpath("//button[normalize-space()='Comment']");
    private final By COMMENT_BLOCKS = By.xpath("//div[@class='add-comment py-4']");
    private final By REVIEW_BLOCKS = By.xpath("//ul[@class='review-comment-list']");
    // --- region: Review list in UL.review-comment-list ---
    private final By REVIEW_LIST_ITEMS = By.xpath("//ul[@class='review-comment-list']/li");
    private final By SEARCH_INPUT = By.xpath("//input[contains(@placeholder,'Search reviews')]");
    //Star
    private final By STAR_ITEMS = By.cssSelector("ul.ant-rate li.ant-rate-star");  // 5 sao
    private final By STAR_SECOND = By.cssSelector(".ant-rate-star-second");
    private final By STAR_RADIO = By.cssSelector("[role='radio']"); // nơi cần click
    private final By ratingWrapSel = By.cssSelector(".ant-rate, .rating, .rate");
    private final By fullStarSel = By.cssSelector(".ant-rate-star-full, .star.filled, [data-filled='true']");
    private final By halfStarSel = By.cssSelector(".ant-rate-star-half, .star.half, [data-half='true']");
    private final By ratingTextSel = By.cssSelector(".rating-text, .rate-value, [aria-label*='rating']");

    private final By helpfulYes = By.xpath(".//*[self::button or self::a or self::span or self::div][contains(normalize-space(),'Yes') and not(contains(@class,'disabled'))]");
    private final By helpfulNo = By.xpath(".//*[self::button or self::a or self::span or self::div][contains(normalize-space(),'No') and not(contains(@class,'disabled'))]");
    private final By HELPFUL_ITEM = By.xpath("//ul[@class='review-comment-list']//li[.//*[contains(normalize-space(),'Helpful')]]");
    private final By comparePackages = By.xpath("(//*[self::a or self::button][normalize-space()='Compare Packages'])[1]");
    private final By contactMe = By.xpath("(//button[normalize-space()='Contact Me'])");
    private final By CHAT_ROOT = By.xpath("(" +
            "//*[contains(@class,'chat') or contains(@class,'messag') or contains(@class,'inbox') or @role='dialog']" + "[.//*]" + ")[1]");
    // ====== About The Seller → Contact Me ======
    private final By CONTACT_ME_UNDER_ABOUT_SELLER = By.xpath(
            "//h2[normalize-space()='About The Seller']" +
                    "/following::*[self::button or self::a][normalize-space()='Contact Me'][1]"
    );
    private final By reviewsHeader = By.xpath("(//*[contains(.,'Reviews')])[1]");
    // FAQ
    private final By faqFirstCheckbox = By.cssSelector(".FAQ ul li:nth-of-type(1) > input[type='checkbox']");
    private final By faqFirstContent = By.cssSelector(".FAQ ul li:nth-of-type(1) > p");


    // ===== Actions =====


    private String getActivePaneId() {
        var panes = driver.findElements(ACTIVE_PANE);
        return panes.isEmpty() ? "" : String.valueOf(panes.get(0).getAttribute("id"));
    }

    public String getActivePackageTitle() {
        var panes = driver.findElements(ACTIVE_PANE);
        if (panes.isEmpty()) return "";
        var titles = panes.get(0).findElements(PANE_TITLE);
        return titles.isEmpty() ? "" : titles.get(0).getText().trim();
    }

    private boolean isDisabled(WebElement el) {
        String cls = String.valueOf(el.getAttribute("class")).toLowerCase();
        return "true".equalsIgnoreCase(el.getAttribute("aria-disabled"))
                || el.getAttribute("disabled") != null
                || cls.contains("disabled");
    }


    /**
     * THỬ click tab package và đợi panel đổi; false nếu tab disabled hoặc panel không đổi
     */
    public boolean clickPackageAndVerifyChanged(String name, Duration timeout) {
        WebElement tab = new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.presenceOfElementLocated(packageTab(name)));
        if (isDisabled(tab)) return false;

        String beforeId = getActivePaneId();
        String beforeTitle = getActivePackageTitle();

        safeClick(tab);

        try {
            return new WebDriverWait(driver, timeout).until(d -> {
                String afterId = getActivePaneId();
                String afterTitle = getActivePackageTitle();
                boolean idChanged = !afterId.isBlank() && !afterId.equalsIgnoreCase(beforeId);
                boolean titleContains = afterTitle.toLowerCase().contains(name.toLowerCase());
                return idChanged || titleContains;
            });
        } catch (TimeoutException e) {
            return false;
        }
    }

    //Click continue
    public void clickContinue() {
        waitPageReady();
        WebElement btn = new WebDriverWait(driver, Duration.ofSeconds(12)).until(ExpectedConditions.presenceOfElementLocated(continueBtn));
        scrollCenter(btn);
        safeClick(btn);
        System.out.println("đã click btn");
    }


    // Mở FAQ đầu tiên và trả về text nội dung

    public String openFirstFaqAndGetText() {
        waitPageReady();
        WebDriverWait w = new WebDriverWait(driver, Duration.ofSeconds(12));
        WebElement toggle = w.until(ExpectedConditions.presenceOfElementLocated(faqFirstCheckbox));
        scrollCenter(toggle);

        // Chỉ click khi chưa mở
        boolean opened = "true".equalsIgnoreCase(String.valueOf(toggle.getAttribute("aria-expanded"))) || isInOpenedDetails(toggle);
        if (!opened) safeClick(toggle);

        // Tìm vùng nội dung: ưu tiên theo aria-controls, rồi fallback theo selector
        WebElement content = findContentForToggle(toggle);
        if (content == null) {
            content = w.until(ExpectedConditions.presenceOfElementLocated(faqFirstContent));
        }

        // biến final để dùng trong lambda
        final WebElement contentEl = content;
        w.until(d -> isVisible(contentEl) && getClientHeight(contentEl) > 0); // từ BasePage
        return getInnerText(contentEl).trim();
    }


    private boolean isInOpenedDetails(WebElement toggle) {
        try {
            WebElement details = (WebElement) ((JavascriptExecutor) driver)
                    .executeScript("return arguments[0].closest('details')", toggle);
            return details != null && "true".equalsIgnoreCase(String.valueOf(details.getAttribute("open")));
        } catch (Exception e) {
            return false;
        }
    }

    private WebElement findContentForToggle(WebElement toggle) {
        String controls = toggle.getAttribute("aria-controls");
        if (controls != null && !controls.isBlank()) {
            List<WebElement> byId = driver.findElements(By.id(controls));
            if (!byId.isEmpty()) return byId.get(0);
        }
        // details/summary → lấy phần không phải summary bên trong details
        try {
            WebElement details = (WebElement) ((JavascriptExecutor) driver)
                    .executeScript("return arguments[0].closest('details')", toggle);
            if (details != null) {
                List<WebElement> candidates = details.findElements(faqFirstContent);
                if (!candidates.isEmpty()) return candidates.get(0);
                List<WebElement> children = details.findElements(By.xpath("./*"));
                for (WebElement el : children) {
                    if (!"summary".equalsIgnoreCase(el.getTagName())) return el;
                }
            }
        } catch (Exception ignore) {
        }
        return null;
    }


    // Trả về true nếu textarea comment đang invalid (bị required)
    public boolean isCommentFieldInvalid() {
        WebElement ta = wait.until(ExpectedConditions.visibilityOfElementLocated(commentBox));
        return !Boolean.TRUE.equals(
                ((JavascriptExecutor) driver).executeScript("return arguments[0].checkValidity();", ta)
        );
    }


    /**
     * Chờ KHÔNG có review mới xuất hiện (true = không tăng, false = tăng)
     */
    public boolean waitNoNewReview(int beforeCount, java.time.Duration timeout) {
        try {
            new org.openqa.selenium.support.ui.WebDriverWait(driver, timeout)
                    .until(d -> getReviewCount() > beforeCount);
            return false; // có tăng -> không mong muốn
        } catch (org.openqa.selenium.TimeoutException ignored) {
            return true;  // không tăng -> đúng kỳ vọng khi lỗi
        }
    }

    /**
     * Tìm thông điệp lỗi liên quan "quá dài/giới hạn" (inline/toast/validationMessage). Trả về chuỗi rỗng nếu không thấy.
     */
    public String findTooLongErrorText(java.time.Duration timeout) {
        final java.util.regex.Pattern PAT = java.util.regex.Pattern.compile(
                "(too|exceed|over|limit|max|character|characters|kí tự|ký tự|vượt|quá)",
                java.util.regex.Pattern.CASE_INSENSITIVE | java.util.regex.Pattern.UNICODE_CASE
        );

        // 1) HTML5 validationMessage (nếu có)
        try {
            String vm = getCommentValidationMessage();
            if (vm != null && PAT.matcher(vm).find()) return vm.trim();
        } catch (Exception ignored) {
        }

        org.openqa.selenium.support.ui.WebDriverWait w =
                new org.openqa.selenium.support.ui.WebDriverWait(driver, timeout);

        // 2) Inline/Toast phổ biến
        try {
            return w.until(d -> {
                String html = d.getPageSource();
                if (html != null && PAT.matcher(html).find()) {
                    // Trả gọn thông điệp khớp (không cần quá đẹp, chỉ để assert)
                    return "page contains: " + PAT.matcher(html).pattern();
                }
                return null;
            });
        } catch (org.openqa.selenium.TimeoutException ignored) {
            // 3) Không thấy -> trả rỗng
            return "";
        }
    }

    public boolean awaitReviewAppears(String expectedText, int beforeCount) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        // vòng 1: không refresh
        try {
            boolean ok = wait.until(d ->
                    getReviewCount() > beforeCount ||
                            d.findElements(REVIEW_BLOCKS).stream().anyMatch(el -> {
                                try {
                                    return el.isDisplayed() && el.getText().contains(expectedText);
                                } catch (Exception ex) {
                                    return false;
                                }
                            })
            );
            if (ok) return true;
        } catch (TimeoutException ignore) {
        }
        // vòng 2: refresh 1 lần rồi đợi lại (nhiều site chỉ append sau reload)
        refresh();
        waitPageReady();
        try {
            return wait.until(d ->
                    d.findElements(REVIEW_BLOCKS).stream().anyMatch(el -> {
                        try {
                            return el.isDisplayed() && el.getText().contains(expectedText);
                        } catch (Exception ex) {
                            return false;
                        }
                    })
            );
        } catch (TimeoutException e) {
            return false;
        }
    }


    // === HÀM CHÍNH (đề xuất) ===
    public String getReviewTextByContent(String expected) {
        return driver.findElements(REVIEW_BLOCKS).stream()
                .filter(WebElement::isDisplayed)
                .map(WebElement::getText)
                .filter(t -> t != null && t.contains(expected))
                .findFirst()
                .map(t -> xq(t))
                .orElse("");
    }


    //    // Escape text để đưa vào XPath literal
    private static String xq(String s) {
        if (!s.contains("'")) return "'" + s + "'";
        return "concat('" + s.replace("'", "',\"'\",'") + "')";
    }

    // Tìm ul.ant-rate gần nhất với textarea comment
    private WebElement ratingRootNearComment() {
        WebElement ta = wait.until(ExpectedConditions.presenceOfElementLocated(commentBox));
        JavascriptExecutor js = (JavascriptExecutor) driver;
        WebElement root = (WebElement) js.executeScript(
                "const ta=arguments[0];" +
                        "const rates=[...document.querySelectorAll('ul.ant-rate')];" +
                        "if(!rates.length) return null;" +
                        "const tr=ta.getBoundingClientRect();" +
                        "let best=null, dist=1e9;" +
                        "for(const r of rates){ const rr=r.getBoundingClientRect();" +
                        "  const d=Math.abs(rr.top - tr.top) + Math.abs(rr.left - tr.left);" + // gần theo vị trí
                        "  if(d < dist){ dist=d; best=r; } }" +
                        "return best;", ta
        );
        if (root == null) throw new NoSuchElementException("Không tìm thấy ul.ant-rate gần ô comment");
        return root;
    }

    public void setRating(int stars) {
        final int targetStars = Math.max(1, Math.min(5, stars));

        WebDriverWait w = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement root = w.until(d -> {
            WebElement r = ratingRootNearComment();
            return (r != null && r.isDisplayed()) ? r : null;
        });

        if (String.valueOf(root.getAttribute("class")).contains("ant-rate-disabled")) {
            throw new IllegalStateException("Rating đang disabled");
        }

        List<WebElement> items = root.findElements(STAR_ITEMS);                  // li.ant-rate-star
        if (items.size() < targetStars) throw new NoSuchElementException("Không đủ sao trong ant-rate");
        WebElement item = items.get(targetStars - 1);

        List<WebElement> radios = item.findElements(STAR_RADIO);                 // [role=radio]
        WebElement clickable = !radios.isEmpty()
                ? radios.get(0)
                : item.findElements(STAR_SECOND).stream().findFirst().orElse(item); // .ant-rate-star-second

        scrollCenter(clickable);
        try {
            w.until(ExpectedConditions.elementToBeClickable(clickable)).click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", clickable);
        }

        // xác nhận (refetch vì React có thể re-render)
        new WebDriverWait(driver, Duration.ofSeconds(5)).until(d -> {
            WebElement root2 = ratingRootNearComment();
            WebElement tgt = root2.findElements(STAR_ITEMS).get(targetStars - 1);
            boolean full = String.valueOf(tgt.getAttribute("class")).contains("ant-rate-star-full");
            List<WebElement> r2 = tgt.findElements(STAR_RADIO);
            boolean aria = !r2.isEmpty() && "true".equalsIgnoreCase(r2.get(0).getAttribute("aria-checked"));
            return full || aria;
        });
    }


    // HTML5 validity helpers
    public boolean isCommentValid() {
        WebElement ta = driver.findElement(commentBox);
        Object ok = ((JavascriptExecutor) driver)
                .executeScript("return arguments[0].checkValidity();", ta);
        return Boolean.TRUE.equals(ok);
    }

    public String getCommentValidationMessage() {
        WebElement ta = driver.findElement(commentBox);
        // trigger lại bubble (nếu cần)
        ((JavascriptExecutor) driver).executeScript("arguments[0].reportValidity();", ta);
        Object msg = ((JavascriptExecutor) driver)
                .executeScript("return arguments[0].validationMessage || '';", ta);
        return String.valueOf(msg).trim();
    }

    /// /////so sánh sao


    //Chờ thấy vùng rating và trả wrap hiển thị (ưu tiên cái visible)
    public WebElement waitForRatingVisible() {
        wait.until(ExpectedConditions.presenceOfElementLocated(ratingWrapSel));
        var wraps = driver.findElements(ratingWrapSel);
        for (WebElement w : wraps) if (w.isDisplayed()) return w;
        return wraps.isEmpty() ? null : wraps.get(0);
    }

    /**
     * Đếm sao full trong wrap
     */
    public int countFullStars() {
        WebElement wrap = waitForRatingVisible();
        if (wrap == null) return 0;
        return (int) wrap.findElements(fullStarSel).stream()
                .filter(WebElement::isDisplayed).count();
    }

    /**
     * Đếm sao half trong wrap (nếu có)
     */
    public int countHalfStars() {
        WebElement wrap = waitForRatingVisible();
        if (wrap == null) return 0;
        return (int) wrap.findElements(halfStarSel).stream()
                .filter(WebElement::isDisplayed).count();
    }

    /**
     * Số sao hiển thị: full + (half ? 0.5 : 0), tối đa 5.0
     */
    public double getDisplayedStars() {
        int full = countFullStars();
        int half = countHalfStars();
        double v = full + (half > 0 ? 0.5 : 0.0);
        return Math.min(5.0, v);
    }

    /**
     * Cố gắng đọc rating dạng text (hỗ trợ dấu phẩy & aria-label). Trả null nếu không đọc được
     */
    public Double getParsedRatingText() {
        WebElement wrap = waitForRatingVisible();
        if (wrap == null) return null;

        String raw = null;
        var texts = wrap.findElements(ratingTextSel);
        if (!texts.isEmpty()) {
            raw = texts.get(0).getAttribute("aria-label");
            if (raw == null || raw.isBlank()) raw = texts.get(0).getText();
        }
        if (raw == null) return null;

        var m = java.util.regex.Pattern.compile("(\\d+(?:[\\.,]\\d+)?)").matcher(raw);
        if (m.find()) {
            try {
                return Double.parseDouble(m.group(1).replace(',', '.'));
            } catch (Exception ignored) { /* fall-through */ }
        }
        return null;
    }

    /**
     * In ra thông tin rating (console + TestNG report) và trả string log
     */
    public String logRatingSnapshot() {
        int full = countFullStars();
        int half = countHalfStars();
        double displayed = getDisplayedStars();
        Double parsed = getParsedRatingText();
        Double roundedToHalf = (parsed != null) ? Math.round(parsed * 2.0) / 2.0 : null;

        String line = "[JOB-09] full=" + full
                + ", half=" + half
                + ", displayed=" + displayed
                + (parsed != null ? ", parsed=" + parsed + ", roundedToHalf=" + roundedToHalf : ", parsed=null");

        System.out.println(line);
        try {
            org.testng.Reporter.log(line, true);
        } catch (Throwable ignored) {
        }
        return line;
    }

    /// case10
// --- Review items & star score (dựa theo HTML ở ảnh) ---
    private final By REVIEW_ITEMS = By.cssSelector(
            ".review-comment-list li, .review-item, [data-role='review']"
    );
    private final By STAR_SCORE_IN_REVIEW = By.cssSelector(
            ".star-score, [class*='star-score']" // an toàn nếu class hơi khác
    );

    // Lấy danh sách review đang hiển thị
    private List<WebElement> getVisibleReviews() {
        return driver.findElements(REVIEW_ITEMS).stream()
                .filter(WebElement::isDisplayed).toList();
    }

    // Đọc số sao dạng số trong mỗi review (ví dụ 2 từ <span class="star-score">2</span>)
    private int readNumericScore(WebElement review) {
        List<WebElement> el = review.findElements(STAR_SCORE_IN_REVIEW);
        if (!el.isEmpty() && el.get(0).isDisplayed()) {
            String t = el.get(0).getText();           // "2", "5", đôi khi "2★"
            java.util.regex.Matcher m = java.util.regex.Pattern
                    .compile("(\\d+)").matcher(t == null ? "" : t);
            if (m.find()) return Integer.parseInt(m.group(1));
        }
        // Fallback: tìm "2 Stars" trong text nếu không có span
        java.util.regex.Matcher m2 = java.util.regex.Pattern
                .compile("(\\d)\\s*star", java.util.regex.Pattern.CASE_INSENSITIVE)
                .matcher(review.getText());
        if (m2.find()) return Integer.parseInt(m2.group(1));
        return -1; // không đọc được
    }

    // TẤT CẢ review (và phải đọc được ÍT NHẤT 1 cái) có điểm bằng n
    public boolean allReviewsHaveNumericScore(int n) {
        int readable = 0;
        for (WebElement r : getVisibleReviews()) {
            int s = readNumericScore(r);
            if (s < 0) continue;   // không đọc được -> bỏ qua
            readable++;
            if (s != n) return false;
        }
        return readable > 0;       // bắt buộc đọc được >=1 review
    }

    // Breakdown button "n Stars" (theo ảnh là <button> trong .rating-section)
    private By starBreakdownBtn(int n) {
        return By.xpath("//div[contains(@class,'rating-section')]//button[normalize-space()='" + n + " Stars']");
    }

    // Click breakdown và chờ áp dụng filter
    public void filterByStarBreakdown(int n) {
        waitPageReady();
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(starBreakdownBtn(n)));
        int before = getReviewCount();
        WebElement firstBefore = getVisibleReviews().stream().findFirst().orElse(null);

        scrollCenter(btn);
        safeClick(btn);
        System.out.println("đã click");
        new WebDriverWait(driver, Duration.ofSeconds(12))
                .ignoring(org.openqa.selenium.StaleElementReferenceException.class)
                .until(d -> {
                    boolean countChanged = getReviewCount() != before;
                    boolean okNumeric = allReviewsHaveNumericScore(n);
                    boolean firstStale = firstBefore != null &&
                            java.util.Objects.equals(
                                    org.openqa.selenium.support.ui.ExpectedConditions.stalenessOf(firstBefore).apply(d),
                                    Boolean.TRUE
                            );
                    return countChanged || okNumeric || firstStale;
                });
        waitOverlaysGone();
    }


    //end10


    //Case 11

    // Chuẩn hóa text (lowercase + bỏ dấu + gọn khoảng trắng)
    private String norm(String s) {
        if (s == null) return "";
        String t = java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
        return t.replaceAll("\\s+", " ").trim().toLowerCase();
    }

    // Gõ và submit keyword
    public void searchReviewsTypeAndSubmit(String k) {
        WebElement box = new WebDriverWait(driver, Duration.ofSeconds(8))
                .until(ExpectedConditions.visibilityOfElementLocated(SEARCH_INPUT));
        scrollCenter(box);
        safeClick(box);
        box.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
        box.clear();
        box.sendKeys(k);
        new WebDriverWait(driver, Duration.ofSeconds(3))
                .until(d -> norm(box.getAttribute("value")).equals(norm(k)));
        box.sendKeys(Keys.ENTER);
    }

    // Chờ kết quả áp dụng với phạm vi UL.review-comment-list
    public void searchReviewsAndWaitInList(String keyword) {
        waitPageReady();
        // Đảm bảo UL xuất hiện
        new WebDriverWait(driver, Duration.ofSeconds(8))
                .until(ExpectedConditions.presenceOfElementLocated(REVIEW_BLOCKS));

        int before = driver.findElements(REVIEW_LIST_ITEMS).size();
        WebElement firstBefore = driver.findElements(REVIEW_LIST_ITEMS).stream()
                .filter(WebElement::isDisplayed).findFirst().orElse(null);

        searchReviewsTypeAndSubmit(keyword);

        String needle = norm(keyword);
        new WebDriverWait(driver, Duration.ofSeconds(12))
                .ignoring(StaleElementReferenceException.class)
                .until(d -> {
                    List<WebElement> items = d.findElements(REVIEW_LIST_ITEMS);
                    boolean countChanged = items.size() != before;
                    boolean firstStale = firstBefore != null &&
                            Boolean.TRUE.equals(ExpectedConditions.stalenessOf(firstBefore).apply(d));
                    boolean allContain = items.stream()
                            .filter(WebElement::isDisplayed)
                            .map(WebElement::getText)
                            .map(this::norm)
                            .allMatch(t -> t.contains(needle));
                    return countChanged || firstStale || allContain;
                });
        waitOverlaysGone();
    }

    public int countVisibleListItems() {
        return (int) driver.findElements(REVIEW_LIST_ITEMS).stream()
                .filter(WebElement::isDisplayed).count();
    }

    // Tất cả <li> hiển thị trong UL đều chứa keyword
    public boolean allListItemsContain(String keyword) {
        String needle = norm(keyword);
        int checked = 0;
        for (WebElement li : driver.findElements(REVIEW_LIST_ITEMS)) {
            if (!li.isDisplayed()) continue;
            checked++;
            if (!norm(li.getText()).contains(needle)) return false;
        }
        return checked > 0; // bắt buộc có ít nhất 1 kết quả
    }

    //===Case 12a,b,c===
    // lấy block helpful đầu tiên trong list
    private WebElement firstHelpfulBlock() {
        new WebDriverWait(driver, Duration.ofSeconds(12))
                .until(ExpectedConditions.presenceOfElementLocated(REVIEW_BLOCKS));
        return new WebDriverWait(driver, Duration.ofSeconds(12))
                .until(ExpectedConditions.visibilityOfElementLocated(HELPFUL_ITEM));
    }

    // Click YES (trong list)
    public void voteHelpfulYes() {
        waitPageReady();
        WebElement li = firstHelpfulBlock();
        WebDriverWait w = new WebDriverWait(driver, Duration.ofSeconds(12));
        w.until(ExpectedConditions.presenceOfElementLocated(REVIEW_BLOCKS));
        WebElement btnyes = w.until(ExpectedConditions.presenceOfNestedElementLocatedBy(li, helpfulYes));
        scrollCenter(btnyes);
        safeClick(btnyes);
        System.out.println("Đã click btn ");
    }

    // Click NO (trong list)
    public void voteHelpfulNo() {
        waitPageReady();
        WebElement li = firstHelpfulBlock();
        WebDriverWait w = new WebDriverWait(driver, Duration.ofSeconds(12));
        w.until(ExpectedConditions.presenceOfElementLocated(REVIEW_BLOCKS));
        WebElement btnno = w.until(ExpectedConditions.presenceOfNestedElementLocatedBy(li, helpfulNo));
        scrollCenter(btnno);
        safeClick(btnno);
        System.out.println("Đã click btn");
    }

    //Đợi màu/class/aria của YES đổi
    public boolean waitHelpfulYesToggled(Duration timeout) {
        WebDriverWait w = new WebDriverWait(driver, timeout);
        return w.ignoring(StaleElementReferenceException.class).until(d -> {
            // re-locate block + yes MỖI LẦN
            WebElement li = d.findElements(By.xpath("//ul[@class='review-comment-list']//li[.//*[contains(normalize-space(),'Helpful')]]"))
                    .stream().filter(WebElement::isDisplayed).findFirst().orElse(null);
            if (li == null) return false;

            List<WebElement> yesL = li.findElements(By.xpath(".//*[self::button or self::a or self::span or self::div][normalize-space()='Yes']"));
            if (yesL.isEmpty()) return false;
            WebElement yes = yesL.get(0);

            // các tín hiệu đổi trạng thái
            boolean textShown = !li.findElements(By.xpath(".//*[contains(normalize-space(),'You found this review helpful')]")).isEmpty();

            String cls = String.valueOf(yes.getAttribute("class")).toLowerCase();
            boolean classChanged = cls.contains("active") || cls.contains("selected") || cls.contains("text-success");

            String aria = String.valueOf(yes.getAttribute("aria-pressed"));
            boolean ariaPressed = "true".equalsIgnoreCase(aria);

            // icon fill/color
            boolean iconColorChanged = false;
            try {
                WebElement icon = yes.findElement(By.xpath(".//*[self::svg or self::i]"));
                String fill = icon.getCssValue("fill");
                if (fill == null || fill.isBlank()) fill = icon.getCssValue("color");
                // nếu icon có màu xanh (thường rgba(..., g>r))
                iconColorChanged = fill != null && !fill.isBlank() && !fill.equalsIgnoreCase("rgba(0, 0, 0, 1)") && !fill.equalsIgnoreCase("rgb(0, 0, 0)");
            } catch (Exception ignore) {
            }

            return textShown || classChanged || ariaPressed || iconColorChanged;
        });
    }

    // Đợi màu/class/aria của NO đổi
    public boolean waitHelpfulNoToggled(Duration timeout) {
        WebDriverWait w = new WebDriverWait(driver, timeout);
        return w.ignoring(StaleElementReferenceException.class).until(d -> {
            WebElement li = d.findElements(By.xpath("//ul[@class='review-comment-list']//li[.//*[contains(normalize-space(),'Helpful')]]"))
                    .stream().filter(WebElement::isDisplayed).findFirst().orElse(null);
            if (li == null) return false;

            List<WebElement> noL = li.findElements(By.xpath(".//*[self::button or self::a or self::span or self::div][normalize-space()='No']"));
            if (noL.isEmpty()) return false;
            WebElement no = noL.get(0);

            boolean textGone = li.findElements(By.xpath(".//*[contains(normalize-space(),'You found this review helpful')]")).isEmpty();

            String cls = String.valueOf(no.getAttribute("class")).toLowerCase();
            boolean classChanged = cls.contains("active") || cls.contains("selected") || cls.contains("text-danger");

            String aria = String.valueOf(no.getAttribute("aria-pressed"));
            boolean ariaPressed = "true".equalsIgnoreCase(aria);

            boolean iconColorChanged = false;
            try {
                WebElement icon = no.findElement(By.xpath(".//*[self::svg or self::i]"));
                String fill = icon.getCssValue("fill");
                if (fill == null || fill.isBlank()) fill = icon.getCssValue("color");
                iconColorChanged = fill != null && !fill.isBlank() && !fill.equalsIgnoreCase("rgba(0, 0, 0, 1)") && !fill.equalsIgnoreCase("rgb(0, 0, 0)");
            } catch (Exception ignore) {
            }

            return (textGone && (classChanged || ariaPressed || iconColorChanged)) || classChanged || ariaPressed || iconColorChanged;
        });
    }

    /// end


    public void openComparePackages() {
        waitPageReady();
        WebDriverWait w = new WebDriverWait(driver, Duration.ofSeconds(12));
        WebElement compare = w.until(ExpectedConditions.presenceOfElementLocated(comparePackages));
        scrollCenter(compare);
        waitOverlaysGone();
        safeClick(compare);
        System.out.println("Đã click btn");
    }

    //Click contactme
    public void clickContactMe() {
        waitPageReady();
        WebDriverWait w = new WebDriverWait(driver, Duration.ofSeconds(12));
        List<WebElement> cand = driver.findElements(CONTACT_ME_UNDER_ABOUT_SELLER);
        WebElement contact = !cand.isEmpty() ? cand.get(0)
                : w.until(ExpectedConditions.presenceOfElementLocated(contactMe));
        scrollCenter(contact);
        waitOverlaysGone();
        safeClick(contact);
        System.out.println("Đã click btn");
    }

    // Mở chat trong cùng tab
    public void clickContactMe_sameTab() {
        waitPageReady();
        clickContactMe();
    }

    // Chờ chat hiển thị (modal/panel)
    public boolean waitForChatVisible(Duration timeout) {
        try {
            new WebDriverWait(driver, timeout)
                    .until(ExpectedConditions.visibilityOfElementLocated(CHAT_ROOT));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    // Tiện ích: đảm bảo KHÔNG mở tab mới và chat hiện trong cùng tab
    public boolean openModalWithoutNewTab(Duration timeout) {
        waitPageReady();
        java.util.Set<String> before = driver.getWindowHandles();
        clickContactMe_sameTab();

        WebDriverWait w = new WebDriverWait(driver, timeout);
        // 1) KHÔNG tăng số window
        w.until(ExpectedConditions.numberOfWindowsToBe(before.size()));
        // 2) Chat hiện
        return waitForChatVisible(timeout);
    }


    public boolean hasContinueButton() {
        return !driver.findElements(continueBtn).isEmpty();
    }

    public int averageStarsInHeader() {
        try {
            WebElement h = wait.until(ExpectedConditions.visibilityOfElementLocated(reviewsHeader));
            return h.findElements(By.xpath(".//*[contains(@class,'star') and (contains(@class,'active') or contains(@class,'fill'))]")).size();
        } catch (Exception e) {
            return -1;
        }
    }

    public int averageRatingTextValue() {
        try {
            WebElement h = wait.until(ExpectedConditions.visibilityOfElementLocated(reviewsHeader));
            Matcher m = Pattern.compile("(\\d+(?:\\.\\d+)?)").matcher(h.getText());
            if (m.find()) {
                String v = m.group(1);
                if (v.contains(".")) v = v.substring(0, v.indexOf('.'));
                return Integer.parseInt(v);
            }
            return -1;
        } catch (Exception e) {
            return -1;
        }
    }

    public boolean hasValidationError() {
        return !driver.findElements(By.xpath("//*[contains(@class,'error') or contains(.,'required') or contains(.,'bắt buộc') or contains(.,'vui lòng')]")).isEmpty();
    }


    //Đợi bất kỳ toast/alert nào chứa expected (không phân biệt dấu/hoa-thường)


    public boolean continueAndWaitHireSuccess(Duration timeout) {
        // đảm bảo trang sẵn sàng (nếu bạn có waitPageReady thì gọi)
        // waitPageReady();

        WebElement btn = new WebDriverWait(driver, Duration.ofSeconds(12))
                .until(ExpectedConditions.presenceOfElementLocated(continueBtn));
        safeClick(btn);

        final String needle = "thue cong viec thanh cong"; // đã bỏ dấu
        try {
            return new WebDriverWait(driver, timeout)
                    .ignoring(org.openqa.selenium.StaleElementReferenceException.class)
                    .until(d -> {
                        // ưu tiên phần tử toast hiển thị
                        for (WebElement el : d.findElements(TOAST_ANY)) {
                            if (!el.isDisplayed()) continue;
                            String txt = norm(el.getText());
                            if (txt.contains(needle)) return true;
                        }
                        // fallback: tìm trong pageSource
                        return norm(d.getPageSource()).contains(needle);
                    });
        } catch (org.openqa.selenium.TimeoutException e) {
            // debug giúp soi vì sao fail
            System.out.println("[JOB-04b] Không thấy toast. Các candidate:");
            for (WebElement el : driver.findElements(TOAST_ANY)) {
                try {
                    System.out.println(" - " + el.getAttribute("class") + " | " + el.getText());
                } catch (Exception ignore) {
                }
            }
            return false;
        }
    }


    //Count review

    public int getReviewCount() {
        return (int) driver.findElements(REVIEW_BLOCKS).stream().filter(WebElement::isDisplayed).count();
    }

    public void fillComment(String text) {
        WebDriverWait w = new WebDriverWait(driver, Duration.ofSeconds(30));
        WebElement box = w.until(ExpectedConditions.visibilityOfElementLocated(commentBox));
        scrollCenter(box);
        box.clear();
        box.sendKeys(text);

    }

    //Submit
    public void submitComment() {
        waitPageReady();
        WebDriverWait w = new WebDriverWait(driver, Duration.ofSeconds(12));
        w.until(ExpectedConditions.presenceOfElementLocated(COMMENT_BLOCKS));
        WebElement ta = w.until(ExpectedConditions.presenceOfElementLocated(commentBox));
        scrollCenter(ta);
        WebElement btn = w.until(ExpectedConditions.presenceOfElementLocated(commentBtn));
        scrollCenter(btn);
        waitOverlaysGone();
        safeClick(btn);
        System.out.println("Đã click btn Comment");
    }


}
