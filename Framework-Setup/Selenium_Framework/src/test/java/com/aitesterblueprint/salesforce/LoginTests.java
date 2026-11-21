package com.aitesterblueprint.salesforce;

import java.time.Duration;
import java.util.UUID;
import io.qameta.allure.testng.AllureTestNg;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(AllureTestNg.class)
public class LoginTests {
    private static final String LOGIN_URL = "https://login.salesforce.com/?locale=in";
    private static final Duration WAIT_TIMEOUT = Duration.ofSeconds(30);

    private WebDriver driver;
    private LoginPage loginPage;

    @BeforeTest
    public void startBrowser() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        try {
            driver = new ChromeDriver(options);
            driver.manage().timeouts().implicitlyWait(Duration.ZERO);
        } catch (WebDriverException exception) {
            throw new IllegalStateException("Unable to start headed Chrome for the Salesforce login tests.", exception);
        }
    }

    @BeforeMethod
    public void openLoginPage() {
        driver.get(LOGIN_URL);
        loginPage = new LoginPage(driver);
        Assert.assertTrue(loginPage.isLoginFormVisible(), "Salesforce login form should be visible.");
    }

    @Test(priority = 1, description = "Reject invalid Salesforce credentials")
    public void rejectsInvalidCredentials() {
        String invalidEmail = System.getenv().getOrDefault(
                "SALESFORCE_INVALID_EMAIL",
                "automation.invalid." + UUID.randomUUID() + "@example.invalid");

        loginPage.setRememberMe(false);
        Assert.assertFalse(loginPage.isRememberMeSelected(), "Remember Me should be unchecked when selected off.");
        loginPage.setRememberMe(true);
        Assert.assertTrue(loginPage.isRememberMeSelected(), "Remember Me should be checked when selected on.");

        loginPage.submitEmail(invalidEmail);
        String errorMessage = loginPage.waitForLoginError();
        Assert.assertEquals(
                errorMessage,
                "We couldn't find your login details.",
                "An unknown email address should be rejected with a visible login error.");
    }

    @Test(priority = 2, description = "Authenticate with valid Salesforce credentials")
    public void acceptsValidCredentials() {
        String username = System.getenv("SALESFORCE_USERNAME");
        String password = System.getenv("SALESFORCE_PASSWORD");
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            throw new SkipException("Set SALESFORCE_USERNAME and SALESFORCE_PASSWORD to run the valid-login test.");
        }

        loginPage.login(username, password);
        new WebDriverWait(driver, WAIT_TIMEOUT)
                .until(ExpectedConditions.not(ExpectedConditions.urlContains("login.salesforce.com")));
        Assert.assertFalse(
                driver.getCurrentUrl().contains("login.salesforce.com"),
                "A successful login should navigate away from the Salesforce login page.");
    }

    @AfterTest(alwaysRun = true)
    public void stopBrowser() {
        if (driver != null) {
            driver.quit();
        }
    }
}
