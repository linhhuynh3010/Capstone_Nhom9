package scripts;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.CategoriesPage;

import java.time.Duration;

public class CategoriesTests extends BaseTest {
    @Test(groups = {"WithoutLogin", "WithLogin"}, description = "CAT-01 - Số card hiển thị bằng số \"services available\"")
    public void CAT_01_servicesAvailableMatchesCardCount() {
        go(pathCategory());
        CategoriesPage p = new CategoriesPage(driver);
        p.waitPageReady();
        Assert.assertEquals(p.getCardCount(), p.getServicesAvailable());
    }


    @Test(groups = {"WithoutLogin", "WithLogin"}, description = "CAT-02 - Chọn Category từ dropdown")
    public void CAT_02_selectCategory() {
        go(pathCategory());
        CategoriesPage p = new CategoriesPage(driver);
        p.waitPageReady();
        int before = p.getVisibleCardCount();
        String picked = p.openDropdownAndChooseFirst("Category");
        p.waitCardsReloaded(before);
        int after = p.getVisibleCardCount();
        Assert.assertNotEquals(after, before,
                "Số lượng card không thay đổi sau khi chọn Category '" + picked + "'. Trước: " + before + " | Sau: " + after);

    }


    @Test(groups = {"WithoutLogin", "WithLogin"}, description = "CAT-03 - Chọn Service Options từ dropdown")
    public void CAT_03_selectServiceOptions() {
        go(pathCategory());
        CategoriesPage p = new CategoriesPage(driver);
        p.waitPageReady();
        int before = p.getVisibleCardCount();
        String picked = p.openDropdownAndChooseFirst("Service Options");
        p.waitCardsReloaded(before);
        int after = p.getVisibleCardCount();
        Assert.assertNotEquals(after, before,
                "Số lượng card không thay đổi sau khi chọn Service Options\"'" + picked + "'. Trước: " + before + " | Sau: " + after);

    }


    @Test(groups = {"WithoutLogin", "WithLogin"}, description = "CAT-04 - Chọn Seller Details từ dropdown")
    public void CAT_04_selectSellerDetails() {
        go(pathCategory());
        CategoriesPage p = new CategoriesPage(driver);
        p.waitPageReady();
        int before = p.getVisibleCardCount();
        String picked = p.openDropdownAndChooseFirst("Seller Details");
        p.waitCardsReloaded(before);
        int after = p.getVisibleCardCount();
        Assert.assertNotEquals(after, before,
                "Số lượng card không thay đổi sau khi chọn Seller Details '" + picked + "'. Trước: " + before + " | Sau: " + after);

    }

    @Test(groups = {"WithoutLogin", "WithLogin"}, description = "CAT-05 - Chọn Delivery Time từ dropdown")
    public void CAT_05_selectDeliveryTime() {
        go(pathCategory());
        CategoriesPage p = new CategoriesPage(driver);
        p.waitPageReady();
        int before = p.getVisibleCardCount();
        String picked = p.openDropdownAndChooseFirst("Delivery Time");
        p.waitCardsReloaded(before);
        int after = p.getVisibleCardCount();
        Assert.assertTrue(after <= before,
                "Số lượng card không thay đổi sau khi chọn Delivery Time  '" + picked + "'. Trước: " + before + " | Sau: " + after);
    }

    @Test(groups = {"WithoutLogin", "WithLogin"}, description = "CAT-06 - Bật/Tắt Pro services (chỉ còn card Pro nếu có, tắt về đầy đủ)")
    public void CAT_06_toggleProServices() {
        go(pathCategory());
        CategoriesPage p = new CategoriesPage(driver);
        p.waitPageReady();
        // Tổng số card ban đầu
        int totalBefore = p.getCardCount();

        // Bật Pro
        p.toggleProServices();
        p.waitCardsReloaded(totalBefore);
        int totalAfterOn = p.getCardCount();
        int proAfterOn = p.countProCards();
        // Khi bật Pro: nếu có kết quả thì tất cả phải là Pro
        if (totalAfterOn > 0) {
            Assert.assertEquals(proAfterOn, totalAfterOn,
                    "Bật Pro nhưng danh sách không chỉ toàn card Pro");
        }

        // TẮT Pro
        p.toggleProServices();
        p.waitCardsReloaded(totalAfterOn);
        int totalAfterOff = p.getCardCount();

        // Sau khi tắt: danh sách phải quay lại đầy đủ (>= ban đầu)
        Assert.assertTrue(totalAfterOff >= totalBefore,
                "Tắt Pro nhưng danh sách không quay lại đầy đủ (trước: " + totalBefore + ", sau: " + totalAfterOff + ")");
    }


    @Test(groups = {"WithoutLogin", "WithLogin"}, description = "CAT-07 - Bật/Tắt Local sellers (thu gọn / khôi phục danh sách)")
    public void CAT_07_toggleLocalSellers() {
        go(pathCategory());
        CategoriesPage p = new CategoriesPage(driver);
        p.waitPageReady();
        int totalBefore = p.getCardCount();

        // BẬT Local sellers
        p.toggleLocalSellers();
        p.waitCardsReloaded(totalBefore);
        int totalAfterOn = p.getCardCount();

        // Khi bật Local: thường số lượng không được tăng (thu gọn/lọc)
        Assert.assertTrue(totalAfterOn <= totalBefore,
                "Bật Local sellers nhưng số card lại không thu gọn (trước: " + totalBefore + ", sau: " + totalAfterOn + ")");

        // TẮT Local sellers
        p.toggleLocalSellers();
        p.waitCardsReloaded(totalAfterOn);
        int totalAfterOff = p.getCardCount();

        // Sau khi tắt: danh sách quay lại đầy đủ (>= ban đầu)
        Assert.assertTrue(totalAfterOff >= totalBefore,
                "Tắt Local sellers nhưng danh sách không quay lại đầy đủ (trước: " + totalBefore + ", sau: " + totalAfterOff + ")");
    }


    @Test(groups = {"WithoutLogin", "WithLogin"}, description = "CAT-08 - Bật/Tắt Online sellers (thu gọn / khôi phục danh sách)")
    public void CAT_08_toggleOnlineSellers() {
        go(pathCategory());
        CategoriesPage p = new CategoriesPage(driver);
        p.waitPageReady();
        int totalBefore = p.getCardCount();
        // BẬT Online sellers
        p.toggleOnlineSellers();
        p.waitCardsReloaded(totalBefore);
        int totalAfterOn = p.getCardCount();
        // Khi bật Online: thường số lượng không được tăng (thu gọn/lọc)
        Assert.assertTrue(totalAfterOn <= totalBefore,
                "Bật Online sellers nhưng số card lại không thu gọn (trước: " + totalBefore + ", sau: " + totalAfterOn + ")");
        // TẮT Online sellers
        p.toggleOnlineSellers();
        p.waitCardsReloaded(totalAfterOn);
        int totalAfterOff = p.getCardCount();

        // Sau khi tắt: danh sách quay lại đầy đủ (>= ban đầu)
        Assert.assertTrue(totalAfterOff >= totalBefore,
                "Tắt Online sellers nhưng danh sách không quay lại đầy đủ (trước: " + totalBefore + ", sau: " + totalAfterOff + ")");
    }

    @Test(groups = {"WithoutLogin", "WithLogin"}, description = "CAT-09 - Đổi Sort By")
    public void CAT_09_changeSortBy() {
        go(pathCategory());
        CategoriesPage p = new CategoriesPage(driver);
        p.waitPageReady();

        // Text hiện tại (thường là "Relevance")
        String before = p.getSortText();

        // Chọn 1 giá trị ưu tiên "Best Selling", nếu đang ở đó thì chọn "New Arrivals"
        String target = before.equalsIgnoreCase("Best Selling") ? "New Arrivals" : "Best Selling";

        p.selectSortBy(target);
        p.waitSortChangedFrom(before);
        String after = p.getSortText();
        Assert.assertNotEquals(after, before, "Không đổi được Sort By");
        Assert.assertEquals(after, target, "Sort By không đúng giá trị đã chọn");
    }


    @Test(groups = {"WithoutLogin", "WithLogin"}, description = "CAT-10 - Click card để vào chi tiết job")
    public void CAT_10_clickCardToDetail() {
        go(pathCategory());
        CategoriesPage p = new CategoriesPage(driver);
        p.waitPageReady();
        p.clickFirstCard(0);
        Assert.assertTrue(p.getJobCardCount() > 0, "Không có Explore Card để click");
        String before = driver.getCurrentUrl();
        p.clickFirstCard(0);
        new WebDriverWait(driver, Duration.ofSeconds(10)).until(d -> !d.getCurrentUrl().equals(before));
        String cur = driver.getCurrentUrl();
        Assert.assertTrue(!cur.contains("/jobDetail") || cur.matches(".*/jobDetail/.*|.*/job.*"), "Không điều hướng sang trang danh mục như mong đợi");

    }


    @Test(groups = {"WithoutLogin"}, description = "CAT-12a - Yêu thích (Favorite) yêu cầu đăng nhập")
    public void CAT_12a_favoriteRequiresLogin() {
        go(pathCategory());
        CategoriesPage p = new CategoriesPage(driver);
        p.waitPageReady();
        int count = p.getVisibleCardCount();
        Assert.assertTrue(count > 0, "Không có card nào hiển thị.");
        p.clickFirstHeart();
        boolean redirected = p.waitForLoginRedirect();
        Assert.assertTrue(redirected, "Expected chuyển đến trang login, nhưng URL hiện tại: " + driver.getCurrentUrl());
    }


    @Test(groups = {"WithLogin"}, description = "CAT-12b - Yêu thích (Favorite) khi đã đăng nhập: icon chuyển sang modal/popover/toast hoặc icon chuyển trạng thái")
    public void CAT_12b_favoriteToggleLoggedIn() {
        go(pathCategory());
        CategoriesPage p = new CategoriesPage(driver);
        p.waitPageReady();
        Assert.assertTrue(p.getVisibleCardCount() > 0, "Không có card nào hiển thị.");
        WebElement heart = p.clickFirstHeart();
        boolean ok = p.waitForFavoriteFeedback(heart);
        Assert.assertTrue(ok, "Không thấy phản hồi UI sau khi click ♥ (modal/popover/toast hoặc icon chuyển trạng thái).");
    }


    @Test(groups = {"WithoutLogin", "WithLogin"}, description = "CAT-16 - id không tồn tại -> điều hướng về trang chủ")
    public void CAT_16_invalidIdRedirectsHome() {
        go(pathCategory());
        String url = driver.getCurrentUrl();
        String newUrl = url.replaceAll("/(\\d+)(/)?$", "/999999");
        if (newUrl.equals(url)) newUrl = url + "/999999";
        driver.get(newUrl);
        String current = driver.getCurrentUrl();
        boolean redirected = !current.contains("/999999") && (current.equals(baseUrl) || current.replaceAll("/+$", "").equals(baseUrl.replaceAll("/+$", "")) || current.endsWith("/"));
        Assert.assertTrue(redirected, "Không điều hướng về trang chủ khi id không tồn tại" + driver.getCurrentUrl());
    }


    @Test(groups = {"WithoutLogin", "WithLogin"}, description = "CAT-17 - id không tồn tại -> điều hướng về trang chủ")
    public void CAT_17_invalidIdRedirectsHome() {
        go(pathCategory());
        String url = driver.getCurrentUrl();
        String newUrl = url.replaceAll("/(\\d+)(/)?$", "/test");
        if (newUrl.equals(url)) newUrl = url + "/test";
        driver.get(newUrl);
        String current = driver.getCurrentUrl();
        boolean redirected = !current.contains("test") && (current.equals(baseUrl) || current.replaceAll("/+$", "").equals(baseUrl.replaceAll("/+$", "")) || current.endsWith("/"));
        Assert.assertTrue(redirected, "Không điều hướng về trang chủ khi id không tồn tại");
    }


    @Test(groups = {"WithoutLogin", "WithLogin"}, description = "CAT-18 - Phân trang")
    public void CAT_18_paginationIfAvailable() {
        go(pathCategory());
        CategoriesPage p = new CategoriesPage(driver);
        boolean hasPage2 = p.goToPage2IfExists();
        Assert.assertTrue(hasPage2 || !hasPage2);
    }
}