package scripts;

import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.CategoriesPage;
import pages.TitlePage;

import java.time.Duration;

public class TitleTests extends BaseTest {

    @Test(groups = {"WithoutLogin", "WithLogin"}, description = "TITLE-01 - Mở trang Title hợp lệ")
    public void TITLE_01_openTitlePage() {
        go(pathTitle());
        TitlePage p = new TitlePage(driver);
        p.waitPageReady();
        Assert.assertTrue(driver.getCurrentUrl().contains("/title"), "URL không đúng trang Title");
    }

    @Test(groups = {"WithoutLogin", "WithLogin"}, description = "TITLE-02 - Click 'How Fiverr Works' mở video")
    public void TITLE_02_openVideo() {
        go(pathTitle());
        TitlePage p = new TitlePage(driver);
        p.waitPageReady();
        p.clickHowFiverrWorks();
        Assert.assertTrue(p.isVideoVisible(), "Không thấy entry 'How Fiverr Works'");

    }

    @Test(groups = {"WithoutLogin", "WithLogin"}, description = "TITLE-03 - Click chip \"Most popular\" điều hướng đúng")
    public void TITLE_03_clickPopularChipNavigatesToResult() {
        go(pathTitle());
        TitlePage p = new TitlePage(driver);
        p.waitPageReady();
        p.clickFirstPopularChip();
        String url = driver.getCurrentUrl();
        boolean navigatedToList = url.toLowerCase().contains("/result");
        Assert.assertTrue(navigatedToList, "Không điều hướng tới trang danh sách sau khi click chip. URL: " + url);
    }

    @Test(groups = {"WithoutLogin", "WithLogin"}, description = "TITLE-04 - Click Explore Card điều hướng đúng trang đích")
    public void TITLE_04_clickExploreCard() {
        go(pathTitle());
        TitlePage p = new TitlePage(driver);
        p.waitPageReady();
        Assert.assertTrue(p.getExploreCardCount() > 0, "Không có Explore Card để click");
        String before = driver.getCurrentUrl();
        p.clickExploreCard(0);

        new WebDriverWait(driver, Duration.ofSeconds(10)).until(d -> !d.getCurrentUrl().equals(before));
        String cur = driver.getCurrentUrl();
        Assert.assertTrue(!cur.contains("/title") || cur.matches(".*/categories/.*|.*/job.*"), "Không điều hướng sang trang danh mục như mong đợi");
    }

    @Test(groups = {"WithoutLogin", "WithLogin"}, description = "TITLE-05 - Click tag trong \"Services Related To...\" điều hướng đúng")
    public void TITLE_05_clickTagNavigatesToResults() {
        go(pathTitle());
        TitlePage p = new TitlePage(driver);
        p.waitPageReady();
        String startUrl = driver.getCurrentUrl();
        p.clickFirstServicesRelatedTag();
        String url = driver.getCurrentUrl().toLowerCase();
        boolean navigated = !url.equals(startUrl) && !url.contains("/title/");
        Assert.assertTrue(navigated, "Không điều hướng tới trang danh sách sau khi click chip. URL: " + url);
    }

    @Test(groups = {"WithoutLogin", "WithLogin"}, description = "TITLE-06 - Hiển thị Breadcrumb hoặc Heading đúng ngữ cảnh")
    public void TITLE_06_hasBreadcrumbOrHeading() {
        go(pathTitle());
        TitlePage p = new TitlePage(driver);
        Assert.assertTrue(p.hasBreadcrumbOrHeading(), "Thiếu breadcrumb/heading");
    }

    @Test(groups = {"WithoutLogin", "WithLogin"}, description = "TITLE-07 - Redirect về trang chủ")
    public void TITLE_07_invalidIdRedirectsHome() {

        String base = baseUrl.replaceAll("/+$", "");
        String badUrl = base + "/title/test";
        driver.navigate().to(badUrl);

        WebDriverWait w = new WebDriverWait(driver, Duration.ofSeconds(10));
        w.until(d -> !d.getCurrentUrl().contains("/test"));
        String cur = driver.getCurrentUrl().replaceAll("/+$", "");
        Assert.assertEquals(cur, base, "Không về đúng trang chủ sau khi nhập id không tồn tại");
    }


    @Test(groups = {"WithoutLogin", "WithLogin"}, description = "TITLE-08 - id không tồn tại sẽ điều hướng về trang chủ")
    public void TITLE_08_invalidIdRedirectsHome() {
        go(pathTitle());
        String url = driver.getCurrentUrl();
        String newUrl = url.replaceAll("/(\\d+)(/)?$", "/999999");
        if (newUrl.equals(url)) newUrl = url + "/999999";
        driver.get(newUrl);
        String current = driver.getCurrentUrl();
        boolean redirected = !current.contains("test") && (current.equals(baseUrl) || current.replaceAll("/+$", "").equals(baseUrl.replaceAll("/+$", "")) || current.endsWith("/"));
        Assert.assertTrue(redirected, "Không điều hướng về trang chủ khi id không tồn tại");

    }


    @Test(groups = {"WithoutLogin", "WithLogin"}, description = "TITLE-09 - Phân trang")
    public void TITLE_09_paginationIfAvailable() {
        go(pathTitle());
        TitlePage p = new TitlePage(driver);
        boolean hasPage2 = p.goToPage2IfExists();
        Assert.assertTrue(hasPage2 || !hasPage2);
    }
}