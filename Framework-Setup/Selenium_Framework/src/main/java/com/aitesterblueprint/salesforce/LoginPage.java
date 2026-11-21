package com.aitesterblueprint.salesforce;

import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class LoginPage {
    private static final Duration WAIT_TIMEOUT = Duration.ofSeconds(20);
    private static final By LOGIN_ERROR =
            By.xpath("//div[normalize-space()=\"We couldn't find your login details.\"]");

    @FindBy(xpath = "//input[@type='email']")
    private WebElement email;

    @FindBy(xpath = "//input[@type='password']")
    private WebElement password;

    @FindBy(xpath = "//button[normalize-space()='Continue']")
    private WebElement continueButton;

    @FindBy(xpath = "//button[normalize-space()='Log In'] | //input[@type='submit']")
    private WebElement loginButton;

    @FindBy(xpath = "//input[@type='checkbox']")
    private WebElement rememberMe;

    @FindBy(xpath = "//input[@type='checkbox']/following-sibling::label")
    private WebElement rememberMeControl;

    private final WebDriverWait wait;

    public LoginPage(WebDriver driver) {
        this.wait = new WebDriverWait(driver, WAIT_TIMEOUT);
        PageFactory.initElements(driver, this);
    }

    public void login(String user, String pass) {
        submitEmail(user);
        wait.until(ExpectedConditions.visibilityOf(password)).sendKeys(pass);
        wait.until(ExpectedConditions.elementToBeClickable(loginButton)).click();
    }

    public void submitEmail(String value) {
        WebElement emailField = wait.until(ExpectedConditions.visibilityOf(email));
        emailField.clear();
        emailField.sendKeys(value);
        wait.until(ExpectedConditions.elementToBeClickable(continueButton)).click();
    }

    public void setRememberMe(boolean selected) {
        WebElement checkbox = wait.until(ExpectedConditions.visibilityOf(rememberMe));
        if (checkbox.isSelected() != selected) {
            wait.until(ExpectedConditions.elementToBeClickable(rememberMeControl)).click();
        }
        wait.until(driver -> rememberMe.isSelected() == selected);
    }

    public boolean isRememberMeSelected() {
        return wait.until(ExpectedConditions.visibilityOf(rememberMe)).isSelected();
    }

    public boolean isLoginFormVisible() {
        return wait.until(ExpectedConditions.visibilityOf(email)).isDisplayed()
                && continueButton.isDisplayed();
    }

    public String waitForLoginError() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(LOGIN_ERROR)).getText();
        } catch (TimeoutException exception) {
            throw new IllegalStateException("Salesforce did not display a login error after invalid credentials.", exception);
        }
    }
}
