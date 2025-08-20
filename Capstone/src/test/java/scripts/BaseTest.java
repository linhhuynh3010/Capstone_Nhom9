package scripts;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.*;
import utils.Env;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;

public class BaseTest {
    protected WebDriver driver;
    protected WebDriverWait wait;
    protected String baseUrl;
    protected String browser;
    static Logger logger = LogManager.getLogger("Testing ");
    // routes + ids
    protected String titleRoute, categoryRoute, detailRoute;
    protected String titleId, categoryId, detailId;

    @Parameters({"baseUrl", "headless", "browser",
            "doLogin", "loginUrl", "username", "password",
            "titleRoute", "categoryRoute", "detailRoute",
            "titleId", "categoryId", "detailId"})
    @BeforeMethod(alwaysRun = true)
    public void setUp(@Optional("https://demo5.cybersoft.edu.vn") String baseUrl,
                      @Optional("true") String headless,
                      @Optional("chrome") String browser,
                      @Optional("false") String doLogin,
                      @Optional("https://demo5.cybersoft.edu.vn/login") String loginUrl,
                      @Optional("") String user,
                      @Optional("") String pass,
                      @Optional("/title/{id}") String titleRoute,
                      @Optional("/categories/{id}") String categoryRoute,
                      @Optional("/jobDetail/{id}") String detailRoute,
                      @Optional("1") String titleId,
                      @Optional("2") String categoryId,
                      @Optional("1") String detailId) {
        this.baseUrl = baseUrl;
        this.browser = browser.toLowerCase();
        this.titleRoute = titleRoute;
        this.categoryRoute = categoryRoute;
        this.detailRoute = detailRoute;
        this.titleId = titleId;
        this.categoryId = categoryId;
        this.detailId = detailId;

        driver = createDriver(this.browser, Boolean.parseBoolean(headless));
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        if (Boolean.parseBoolean(doLogin)) {
            driver.get(loginUrl);
            // login inline (không cần PageObject)
            By email = By.xpath("//input[@type='email' or @name='email' or contains(@placeholder,'Email')]");
            By password = By.xpath("//input[@type='password' or @name='password' or contains(@placeholder,'Password') or contains(@placeholder,'Mật khẩu')]");
            By submit = By.xpath("//button[normalize-space()='Login' or contains(.,'Sign in') or contains(.,'Đăng nhập')]");
            wait.until(ExpectedConditions.visibilityOfElementLocated(email)).sendKeys(user);
            wait.until(ExpectedConditions.visibilityOfElementLocated(password)).sendKeys(pass);
            wait.until(ExpectedConditions.elementToBeClickable(submit)).click();
            System.out.println("login thành công");
        }
    }

    private WebDriver createDriver(String browser, boolean headless) {
        switch (browser) {
            case "firefox" -> {
                WebDriverManager.firefoxdriver().setup();
                FirefoxOptions fo = new FirefoxOptions();
                if (headless) fo.addArguments("-headless");
                return new FirefoxDriver(fo);
            }
            case "edge" -> {
                WebDriverManager.edgedriver().setup();
                EdgeOptions eo = new EdgeOptions();
                if (headless) eo.addArguments("--headless=new");
                return new EdgeDriver(eo);
            }
            case "safari" -> {
                // SafariDriver requires Safari 10+ on macOS, headless not supported
                return new SafariDriver();
            }
            default -> {
                WebDriverManager.chromedriver().setup();
                ChromeOptions co = new ChromeOptions();
                if (headless) co.addArguments("--headless=new");
                co.addArguments("--start-maximized", "--disable-gpu");
                co.setAcceptInsecureCerts(true);
                return new ChromeDriver(co);
            }
        }
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        if (driver != null) driver.quit();
    }

    protected void go(String path) {
        driver.get(baseUrl + path);
    }

    protected String pathTitle() {
        return Env.buildPath(titleRoute, titleId);
    }

    protected String pathCategory() {
        return Env.buildPath(categoryRoute, categoryId);
    }

    protected String pathDetail() {
        return Env.buildPath(detailRoute, detailId);
    }
}
