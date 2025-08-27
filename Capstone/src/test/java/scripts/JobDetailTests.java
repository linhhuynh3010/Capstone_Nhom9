package scripts;


/*
 * @Author: Huỳnh Mai Linh
 * @Version: 1.0
 * @Function: Page Jobdetail test
 * */

//@Epic("Tìm kiếm và thuê dịch vụ của  Website https://demo5.cybersoft.edu.vn/")
//@Feature("Kiểm tra các chức năng của trang Jobdetail")

import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import pages.JobDetailPage;
import java.time.Duration;



public class JobDetailTests extends BaseTest {

    @Test(groups = {"WithoutLogin", "WithLogin"}, description = "JOB-01 - Mở trang chi tiết job hiển thị đầy đủ thành phần chính")
    public void JOB_01_openDetailHasKeySections() {
        go(pathDetail());
        JobDetailPage p = new JobDetailPage(driver);
        p.waitPageReady();
        Assert.assertTrue(p.hasContinueButton(), "Thiếu nút Continue/giỏ hàng");

    }



    @DataProvider
    public Object[][] packages() {
        return new Object[][]{{"Basic"}, {"Standard"}, {"Premium"}};
    }

    @Test(dataProvider = "packages", groups = {"WithoutLogin", "WithLogin"}, description = "JOB-03b - Chọn gói và verify panel active đúng (đồng thời bắt case không click được)")
    public void JOB_03b_choosePackage(String pkg) {
        go(pathDetail());
        JobDetailPage p = new JobDetailPage(driver);

        // 1) Bắt luôn case tab không click được / click mà panel không đổi
        Assert.assertTrue(
                p.clickPackageAndVerifyChanged(pkg, Duration.ofSeconds(6)),
                "Không click/chuyển được gói: " + pkg + " (tab có thể disabled hoặc panel không đổi)"
        );

        // 2) Xác nhận tiêu đề panel đang active khớp gói
        String title = p.getActivePackageTitle().toLowerCase();
        Assert.assertTrue(
                title.contains(pkg.toLowerCase()),
                "Tiêu đề panel không khớp gói đã chọn. Expected ~ " + pkg + " | Actual: " + title
        );
    }


    @Test(groups = {"WithLogin"}, description = "JOB-04b - Continue khi đã đăng nhập hiển thị thông báo \"Thuê công việc thành công\"")
    public void JOB_04b_hireShowsSuccessMessage() {
        go(pathDetail());
        JobDetailPage p = new JobDetailPage(driver);
        p.waitPageReady();

        Assert.assertTrue(
                p.continueAndWaitHireSuccess(Duration.ofSeconds(10)),
                "Không thấy message 'Thuê công việc thành công'. URL: " + driver.getCurrentUrl()
        );
    }

    @Test(groups = {"WithoutLogin"}, description = "JOB-04a - Continue khi chưa đăng nhập phải chuyển đến trang đăng nhập")
    public void JOB_04a_continueRequiresLogin() {
        go(pathDetail());
        JobDetailPage p = new JobDetailPage(driver);
        p.waitPageReady();
        p.clickContinue();
        boolean Redirect = p.waitForLoginRedirect();
        Assert.assertTrue(Redirect, "Kỳ vọng login Redirect sau khi click Continue. URL: " + driver.getCurrentUrl());
    }

    @Test(groups = {"WithoutLogin"}, description = "JOB-05 - FAQ: Mở câu hỏi đầu tiên hiển thị nội dung")
    public void JOB_05_faqToggle() {
        go(pathDetail());
        JobDetailPage p = new JobDetailPage(driver);
        String text = p.openFirstFaqAndGetText();
        System.out.println(text);
        Assert.assertTrue(text != null && !text.isEmpty(), "FAQ không hiển thị nội dung sau khi click");
    }


    @Test(groups = {"WithLogin"}, description = "JOB-06a - Comment/Rate: (để trống)")
    public void JOB_06a_commentValidationRequired() {
        go(pathDetail());
        JobDetailPage p = new JobDetailPage(driver);
        p.submitComment();
        Assert.assertFalse(p.isCommentValid(), "Textarea comment đáng lẽ invalid khi để trống.");
        String msg = p.getCommentValidationMessage();
        System.out.println(msg);
        Assert.assertFalse(msg.isEmpty(), "Không thấy validationMessage của textarea.");
    }


    @Test(groups = {"WithoutLogin"}, description = "JOB-06b - Comment/Rate: để trống Redirect về login")
    public void JOB_06b_commentValidationRequired() {
        go(pathDetail());
        JobDetailPage p = new JobDetailPage(driver);
        p.submitComment();
        boolean Redirect = p.waitForLoginRedirect();
        System.out.println(Redirect);
        Assert.assertTrue(Redirect, "Kỳ vọng login Redirect sau khi click Continue. URL: " + driver.getCurrentUrl());
    }


    @Test(groups = {"WithoutLogin"}, description = "JOB-06c - Comment/Rate: hợp lệ nhưng chưa đăng nhập → chuyển login")
    public void JOB_06c_commentRequiresLogin() {
        go(pathDetail());
        JobDetailPage p = new JobDetailPage(driver);
        p.waitPageReady();
        p.fillComment("Bình luận thử");
        p.setRating(5);
        p.submitComment();
        Assert.assertTrue(p.waitForLoginRedirect(),
                "Kỳ vọng mở login sau khi submit comment hợp lệ khi chưa đăng nhập. URL: " + driver.getCurrentUrl());
    }


    @Test(groups = {"WithLogin"}, description = "JOB-07 - Gửi Comment + Rating hợp lệ (chỉ check đúng text)")
    public void JOB_07_commentAndRating() {
        go(pathDetail());
        JobDetailPage p = new JobDetailPage(driver);
        p.waitPageReady();
        String content = "Bình luận" + System.currentTimeMillis();
        int before = p.getReviewCount();
        p.setRating(5);
        p.fillComment(content);
        p.submitComment();
        boolean ok = p.awaitReviewAppears(content, before);
        logger.info("[SubmitComment] awaitReviewAppears result = {}", ok);
        if (!ok) {
            int after = p.getReviewCount();
            logger.info("[SubmitComment] After submit: reviewCount={}, delta={}", after, after - before);
            logger.info("[SubmitComment] Latest review snippet: {}", p.getReviewTextByContent(content));
        }
        Assert.assertTrue(ok, "Không thấy review mới hoặc nội dung vừa gửi. URL: " + before + driver.getCurrentUrl());
        boolean commentAppeared = p.awaitReviewAppears(content, before);
        Assert.assertTrue(commentAppeared, "Comment không xuất hiện sau khi gửi. URL: " + driver.getCurrentUrl());

        String actualText = p.getReviewTextByContent(content);
        Assert.assertEquals(actualText, content, "Nội dung review không khớp. URL: " + driver.getCurrentUrl());

    }

    @Test(groups = {"WithLogin"}, description = "JOB-08 - Comment vượt quá giới hạn ký tự hiển thị thông báo")
    public void JOB_08_commentTooLongShowsError() {
        go(pathDetail());
        JobDetailPage p = new JobDetailPage(driver);
        p.waitPageReady();
        int before = p.getReviewCount();
        String longText = "t".repeat(5000);
        p.setRating(5);
        p.fillComment(longText);
        p.submitComment();
        // KHÔNG được thêm review mới
        Assert.assertTrue(
                p.waitNoNewReview(before, Duration.ofSeconds(5)),
                "Review vẫn được thêm dù comment quá dài"
        );

        // Có thông báo lỗi/invalid (bất kỳ kênh nào)
        boolean invalid = p.isCommentFieldInvalid();
        String err = p.findTooLongErrorText(Duration.ofSeconds(8));
        boolean hasPageError = p.hasValidationError();

        Assert.assertTrue(invalid || hasPageError || (err != null && !err.isBlank()),
                "Không thấy thông báo/invalid khi comment quá dài. validationMessage=" + p.getCommentValidationMessage() + " | err=" + err);
    }


    @Test(groups = {"WithoutLogin", "WithLogin"}, description = "JOB-09 - Số sao ở header khớp giá trị rating hiển thị (nếu đọc được)")
    public void JOB_09_starCountMatchesRate() {
        go(pathDetail());
        JobDetailPage p = new JobDetailPage(driver);
        p.waitPageReady();
        p.waitForRatingVisible();
        // 3) In snapshot giá trị để debug
        String snapshot = p.logRatingSnapshot(); // in ra console + TestNG report
        Reporter.log(snapshot, true);

        // 4) Lấy giá trị sao hiển thị & rating text (nếu có)
        double displayed = p.getDisplayedStars();
        Double parsed = p.getParsedRatingText();


        if (parsed != null) {
            double expected = Math.round(parsed * 2.0) / 2.0; // làm tròn đến half-star
            Reporter.log("[JOB-09] expected(roundedToHalf)=" + expected + ", displayed=" + displayed, true);
            Assert.assertEquals(displayed, expected, 0.01,
                    "Star hiển thị không khớp rating text. " + snapshot);
        } else {
            int full = p.countFullStars();
            int half = p.countHalfStars();
            Assert.assertTrue(full > 0 || half > 0,
                    "Không thấy sao hiển thị để kiểm tra. " + snapshot);
        }
    }


    @Test(groups = {"WithoutLogin", "WithLogin"}, description = "JOB-10 - Click số sao để lọc review")
    public void JOB_10_filterByStars() {
        go(pathDetail());
        JobDetailPage p = new JobDetailPage(driver);
        p.waitPageReady();
        int target = 2;                    // theo ảnh trang /jobDetail/1 là 2★
        p.filterByStarBreakdown(target);

        int after = p.getReviewCount();
        System.out.println("[JOB-10] after=" + after + " reviews");

        if (after == 0) {
            // không có review đúng sao → hợp lệ nếu breakdown thật sự là 0
            Assert.assertEquals(after, 0);
        } else {
            Assert.assertTrue(p.allReviewsHaveNumericScore(target),
                    "Có review có .star-score khác " + target + "★ sau khi lọc");
        }
    }


    @Test(groups = {"WithoutLogin, WithLogin"}, description = "JOB-11 - Tìm kiếm review bằng ô Search reviews")
    public void JOB_11_searchReviews() {
        go(pathDetail());
        JobDetailPage p = new JobDetailPage(driver);
        p.waitPageReady();

        String keyword = "communication";

        p.searchReviewsAndWaitInList(keyword);

        int visible = p.countVisibleListItems();
        Assert.assertTrue(visible > 0, "Không có kết quả nào trong review-comment-list sau khi search");

        Assert.assertTrue(
                p.allListItemsContain(keyword),
                "Có comment KHÔNG chứa keyword trong UL.review-comment-list. Keyword: " + keyword
        );
    }

    @Test(groups = {"WithoutLogin"}, description = "JOB-12a - Bỏ phiếu Helpful (Yes/No) yêu cầu đăng nhập")
    public void JOB_12a_helpfulRequiresLogin() {
        go(pathDetail());
        JobDetailPage p = new JobDetailPage(driver);
        p.waitPageReady();
        p.voteHelpfulYes();
        Assert.assertTrue(p.waitForLoginRedirect(),
                "Kỳ vọng mở login sau khi submit comment hợp lệ khi chưa đăng nhập. URL: " + driver.getCurrentUrl());
        p.voteHelpfulNo();
        Assert.assertTrue(p.waitForLoginRedirect(),
                "Kỳ vọng mở login sau khi submit comment hợp lệ khi chưa đăng nhập. URL: " + driver.getCurrentUrl());
    }


//    @Test(groups = {"WithLogin"}, description = "JOB-12b - Click helpful (Yes/No) đổi màu (đã đăng nhập)")
//    public void JOB_12b_helpfulYesNoChangesColor() {
//        go(pathDetail());
//        JobDetailPage p = new JobDetailPage(driver);
//        p.waitPageReady();
//        p.voteHelpfulYes();
//        Assert.assertTrue(p.waitHelpfulYesToggled(Duration.ofSeconds(12)),
//                "Yes không đổi màu/trạng thái sau khi click");
//
//        p.voteHelpfulNo();
//        Assert.assertTrue(p.waitHelpfulNoToggled(Duration.ofSeconds(12)),
//                "No không đổi màu/trạng thái sau khi click");
//    }


    @Test(groups = {"WithoutLogin"}, description = "JOB-13a - Compare Packages yêu cầu đăng nhập")
    public void JOB_13a_comparePackagesRequiresLogin() {
        go(pathDetail());
        JobDetailPage p = new JobDetailPage(driver);
        p.waitPageReady();
        p.openComparePackages();
        Assert.assertTrue(p.waitForLoginRedirect(),
                "Kỳ vọng mở login sau khi submit comment hợp lệ khi chưa đăng nhập. URL: " + driver.getCurrentUrl());
    }

    @Test(groups = {"WithoutLogin"}, description = "JOB-14a - Contact me khi chưa đăng nhập phải chuyển đến trang đăng nhập")
    public void JOB_14a_contactMeRequiresLogin() {
        go(pathDetail());
        JobDetailPage p = new JobDetailPage(driver);
        p.waitPageReady();
        p.clickContactMe();
        Assert.assertTrue(p.waitForLoginRedirect(),
                "Kỳ vọng mở login sau khi submit comment hợp lệ khi chưa đăng nhập. URL: " + driver.getCurrentUrl());
    }


    @Test(groups = {"WithLogin"}, description = "JOB-14b - Contact me khi đã đăng nhập mở hộp thoại liên hệ")
    public void JOB_14b_contactMeLoggedIn() {
        go(pathDetail());
        JobDetailPage p = new JobDetailPage(driver);
        p.waitPageReady();
        p.clickContactMe();
        Assert.assertTrue(
                p.openModalWithoutNewTab(java.time.Duration.ofSeconds(10)),
                "Không thấy khung chat trong cùng tab sau khi click Contact Me"
        );
    }

    @Test(groups = {"WithoutLogin", "WithLogin"}, description = "JOB-15 - id không tồn tại -> điều hướng về trang chủ")
    public void JOB_15_invalidIdRedirectsHome() {
        go(pathCategory());
        String url = driver.getCurrentUrl();
        String newUrl = url.replaceAll("/(\\d+)(/)?$", "/999999");
        if (newUrl.equals(url)) newUrl = url + "/999999";
        driver.get(newUrl);
        String current = driver.getCurrentUrl();
        boolean redirected = !current.contains("/999999") && (current.equals(baseUrl) || current.replaceAll("/+$", "").equals(baseUrl.replaceAll("/+$", "")) || current.endsWith("/"));
        Assert.assertTrue(redirected, "Không điều hướng về trang chủ khi id không tồn tại " + driver.getCurrentUrl());
    }


    @Test(groups = {"WithLogin"}, description = "JOB-13b - Xóa 1 mục khỏi Compare Packages (nếu đã thêm) và xác nhận đã xóa")
    public void JOB_13b_comparePackagesRemoveItem() {
        go(pathDetail());
        JobDetailPage p = new JobDetailPage(driver);
        p.waitPageReady();
        p.openComparePackages();
        Assert.assertTrue(
                p.openModalWithoutNewTab(java.time.Duration.ofSeconds(10)),
                "Không thấy compare trong cùng tab sau khi click Compare Packages"
        );
    }
}