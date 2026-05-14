package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class AuthPage extends BasePage {

    private static final String LOGIN_URL = "https://passport.yandex.ru/auth";
    private static final String REGISTRATION_URL = "https://passport.yandex.ru/pwl-yandex/reg";

    private static final By FIRST_NAME = By.xpath("//input[@name='firstname' or @id='firstname']");
    private static final By LAST_NAME = By.xpath("//input[@name='lastname' or @id='lastname']");
    private static final By REGISTRATION_LOGIN = By.xpath("//input[@name='login' or @id='login']");

    private static final By PHONE_INPUT = By.xpath(
            "//input[@type='tel']" +
            " | //input[@name='phone' or @id='phone']" +
            " | //input[contains(@autocomplete,'tel')]" +
            " | //input[contains(@placeholder,'(000)000-00-00')]" +
            " | //input[contains(@placeholder,'\u0422\u0435\u043B\u0435\u0444\u043E\u043D')]" +
            " | //input[contains(@placeholder,'\u0442\u0435\u043B\u0435\u0444\u043E\u043D')]"
    );
    private static final By PHONE_FIELD = By.xpath(
            "//*[@id='app']/div[2]/div/form/div[2]/div[1]/div/div/div/div/span[2]" +
            " | //label[contains(normalize-space(.),'\u0422\u0435\u043B\u0435\u0444\u043E\u043D') or contains(normalize-space(.),'\u0442\u0435\u043B\u0435\u0444\u043E\u043D')]"
    );
    private static final By USERNAME_INPUT = By.xpath(
            "//*[@id='react-aria886129763-\u00ABr1e\u00BB']" +
            " | //input[starts-with(@id,'react-aria') and contains(@id,'r1e')]" +
            " | //input[@name='login' or @id='passp-field-login']" +
            " | //input[@type='email' or @type='text']" +
            " | //input[contains(@placeholder,'Login')]" +
            " | //input[contains(@placeholder,'email')]" +
            " | //input[contains(@placeholder,'\u041B\u043E\u0433\u0438\u043D')]" +
            " | //input[contains(@placeholder,'\u043B\u043E\u0433\u0438\u043D')]"
    );
    private static final By LOGIN_INPUT = By.xpath(
            "//input[@name='login' or @id='passp-field-login' or @type='email' or @type='text']"
    );
    private static final By PASSWORD = By.xpath(
            "//input[@name='password' or @id='password' or @type='password']"
    );
    private static final By PASSWORD_CONFIRM = By.xpath(
            "//input[@name='password_confirm' or @id='password_confirm' or @name='passwordConfirm']"
    );

    private static final By MORE_BUTTON = By.xpath(
            "//button[contains(normalize-space(.),'More')]" +
            " | //button[contains(normalize-space(.),'\u0415\u0449\u0451')]" +
            " | //button[contains(normalize-space(.),'\u0415\u0449\u0435')]" +
            " | //a[contains(normalize-space(.),'\u0415\u0449\u0451')]" +
            " | //a[contains(normalize-space(.),'\u0415\u0449\u0435')]" +
            " | //*[@role='button' and (contains(normalize-space(.),'\u0415\u0449\u0451') or contains(normalize-space(.),'\u0415\u0449\u0435'))]"
    );
    private static final By CREATE_ID_FOR_MYSELF = By.xpath(
            "//a[contains(normalize-space(.),'\u0421\u043E\u0437\u0434\u0430\u0442\u044C ID')]" +
            " | //button[contains(normalize-space(.),'\u0421\u043E\u0437\u0434\u0430\u0442\u044C ID')]" +
            " | //*[starts-with(@id,'react-aria') and contains(@id,'r8')]//span" +
            " | //*[starts-with(@id,'react-aria')]//span[contains(normalize-space(.),'Create an ID for yourself')]"
    );
    private static final By LOGIN_WITH_USERNAME = By.xpath(
            "//*[starts-with(@id,'react-aria')]//span[contains(normalize-space(.),'Log in with username')]" +
            " | //*[starts-with(@id,'react-aria')]//span[contains(normalize-space(.),'\u0412\u043E\u0439\u0442\u0438 \u043F\u043E \u043B\u043E\u0433\u0438\u043D\u0443')]" +
            " | //button[contains(normalize-space(.),'Log in with username')]" +
            " | //button[contains(normalize-space(.),'\u0412\u043E\u0439\u0442\u0438 \u043F\u043E \u043B\u043E\u0433\u0438\u043D\u0443')]" +
            " | //a[contains(normalize-space(.),'Log in with username')]" +
            " | //a[contains(normalize-space(.),'\u0412\u043E\u0439\u0442\u0438 \u043F\u043E \u043B\u043E\u0433\u0438\u043D\u0443')]"
    );
    private static final By NEXT_OR_SUBMIT = By.xpath(
            "//button[contains(normalize-space(.),'\u0414\u0430\u043B\u0435\u0435')]" +
            " | //button[contains(normalize-space(.),'\u041F\u0440\u043E\u0434\u043E\u043B\u0436\u0438\u0442\u044C')]" +
            " | //button[contains(normalize-space(.),'Next')]" +
            " | //button[contains(normalize-space(.),'Continue')]" +
            " | //button[@type='submit']"
    );
    private static final By ERROR = By.xpath(
            "//*[@role='alert']" +
            " | //*[@aria-live='assertive']" +
            " | //*[contains(@class,'error') and normalize-space(.)!='']" +
            " | //*[contains(@class,'invalid') and normalize-space(.)!='']" +
            " | //*[contains(@id,'error') and normalize-space(.)!='']" +
            " | //*[contains(normalize-space(.),'Invalid phone number format')]" +
            " | //*[contains(normalize-space(.),'\u041D\u0435\u0434\u043E\u043F\u0443\u0441\u0442\u0438\u043C\u044B\u0439 \u0444\u043E\u0440\u043C\u0430\u0442 \u043D\u043E\u043C\u0435\u0440\u0430')]" +
            " | //*[starts-with(@id,'react-aria') and contains(@id,'r28')]"
    );
    private static final By SMS_CODE = By.xpath(
            "//input[@name='code' or @type='tel' or @inputmode='numeric']" +
            " | //*[contains(normalize-space(.),'\u043A\u043E\u0434') or contains(normalize-space(.),'\u041A\u043E\u0434')]" +
            " | //*[contains(normalize-space(.),'SMS') or contains(normalize-space(.),'\u0441\u043C\u0441')]"
    );
    private static final By RESEND_BUTTON = By.xpath(
            "//button[contains(normalize-space(.),'Resend')]" +
            " | //button[contains(normalize-space(.),'Отправить ещё раз')]" +
            " | //button[contains(normalize-space(.),'Отправить еще раз')]" +
            " | //button[contains(normalize-space(.),'Выслать код')]" +
            " | //button[contains(normalize-space(.),'выслать код')]" +
            " | //button[contains(normalize-space(.),'resend')]"
    );
    private static final By SMS_MENU_OPTION = By.xpath(
            "//*[contains(normalize-space(.),'SMS') or contains(normalize-space(.),'СМС')]" +
            "[self::button or self::a or self::li or @role='menuitem' or @role='option']"
    );
    private static final By ACCOUNT_MARKER = By.xpath(
            "//*[contains(@data-t,'account')]" +
            " | //a[contains(@href,'passport.yandex') and contains(@href,'profile')]" +
            " | //*[contains(@class,'user') and normalize-space(.)!='']"
    );

    public AuthPage(WebDriver driver) {
        super(driver);
    }

    public AuthPage openRegistration() {
        driver.get(REGISTRATION_URL);
        return this;
    }

    public AuthPage openLogin() {
        driver.get(LOGIN_URL);
        return this;
    }

    public AuthPage openRegistrationFromLogin() {
        waitClickable(MORE_BUTTON).click();
        clickByJs(CREATE_ID_FOR_MYSELF);
        wait.until(ExpectedConditions.or(
                ExpectedConditions.presenceOfElementLocated(PHONE_INPUT),
                ExpectedConditions.presenceOfElementLocated(PHONE_FIELD)
        ));
        return this;
    }

    public AuthPage openLoginByUsername() {
        if (isPresent(USERNAME_INPUT)) {
            return this;
        }
        if (isPresent(MORE_BUTTON)) {
            waitClickable(MORE_BUTTON).click();
            clickByJs(LOGIN_WITH_USERNAME);
        }
        wait.until(ExpectedConditions.presenceOfElementLocated(USERNAME_INPUT));
        return this;
    }

    public AuthPage registerByPhone(String phone) {
        typeInto(PHONE_INPUT, PHONE_FIELD, phone);
        submit();
        return this;
    }

    public AuthPage registerByEmail(String email, String password) {
        typeIfPresent(FIRST_NAME, "Ivan");
        typeIfPresent(LAST_NAME, "Nikitin");
        typeIfPresent(REGISTRATION_LOGIN, email);
        typeIfPresent(PASSWORD, password);
        typeIfPresent(PASSWORD_CONFIRM, password);
        submit();
        return this;
    }

    public AuthPage submitEmptyRegistration() {
        submit();
        return this;
    }

    public AuthPage login(String email, String password) {
        enterUsername(email);
        enterPassword(password);
        return this;
    }

    public AuthPage enterUsername(String username) {
        typeInto(USERNAME_INPUT, null, username);
        submit();
        return this;
    }

    public AuthPage enterPassword(String password) {
        typeInto(PASSWORD, null, password);
        submit();
        return this;
    }

    public AuthPage submitEmptyLogin() {
        submit();
        return this;
    }

    public boolean hasValidationError() {
        try {
            wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(ERROR, 0));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean hasValidationErrorOrSmsChallenge() {
        try {
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.numberOfElementsToBeMoreThan(ERROR, 0),
                    ExpectedConditions.numberOfElementsToBeMoreThan(SMS_CODE, 0)
            ));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public AuthPage enterPhoneAndSubmit(String phone) {
        typeInto(PHONE_INPUT, PHONE_FIELD, phone);
        submit();
        return this;
    }

    // Яндекс сначала показывает код для приложения; через ~30 сек появляется "Resend code"
    public AuthPage waitForSmsButtonAndClick() {
        WebDriverWait resendWait = new WebDriverWait(driver, Duration.ofSeconds(90));
        resendWait.until(ExpectedConditions.elementToBeClickable(RESEND_BUTTON)).click();
        // После клика появляется меню — выбираем "via SMS"
        WebDriverWait menuWait = new WebDriverWait(driver, Duration.ofSeconds(10));
        menuWait.until(ExpectedConditions.elementToBeClickable(SMS_MENU_OPTION)).click();
        return this;
    }

    // Ждём пока пользователь вручную введёт код и страница уйдёт с passport
    public boolean waitForManualLogin(int timeoutSeconds) {
        WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
        try {
            longWait.until(driver -> !driver.getCurrentUrl().contains("passport.yandex"));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isLoggedIn() {
        try {
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.urlContains("profile"),
                    ExpectedConditions.urlContains("passport.yandex.ru/profile"),
                    ExpectedConditions.presenceOfElementLocated(ACCOUNT_MARKER)
            ));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void clickByJs(By locator) {
        WebElement element = waitClickable(locator);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
    }

    private WebElement findVisibleEnabledElement(By locator) {
        try {
            return wait.until(driver -> {
                for (WebElement element : driver.findElements(locator)) {
                    if (element.isDisplayed() && element.isEnabled()) {
                        return element;
                    }
                }
                return null;
            });
        } catch (Exception e) {
            return null;
        }
    }

    private void typeInto(By inputLocator, By fieldLocator, String value) {
        RuntimeException lastError = null;
        for (int attempt = 0; attempt < 3; attempt++) {
            try {
                WebElement input = findVisibleEnabledElement(inputLocator);
                if (input == null && fieldLocator != null && isPresent(fieldLocator)) {
                    waitClickable(fieldLocator).click();
                    input = driver.switchTo().activeElement();
                }
                if (input == null) {
                    input = waitClickable(inputLocator);
                }

                input.click();
                input.clear();
                input.sendKeys(value);
                return;
            } catch (RuntimeException e) {
                lastError = e;
            }
        }
        throw lastError;
    }

    private void typeIfPresent(By locator, String value) {
        List<WebElement> elements = driver.findElements(locator);
        if (!elements.isEmpty()) {
            WebElement input = elements.get(0);
            input.clear();
            input.sendKeys(value);
        }
    }

    private void submit() {
        WebElement submitButton = findVisibleEnabledElement(NEXT_OR_SUBMIT);
        if (submitButton != null) {
            submitButton.click();
            return;
        }
        driver.switchTo().activeElement().sendKeys(Keys.ENTER);
    }
}
