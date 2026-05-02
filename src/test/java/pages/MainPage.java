package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.Set;

public class MainPage extends BasePage {

    public static final String URL = "https://market.yandex.ru";

    private static final By SEARCH_INPUT = By.xpath(
            "//input[@data-auto='search-input-field']" +
            " | //form[contains(@action,'search')]//input[@type='text' or @type='search']" +
            " | //input[contains(@placeholder,'\u0418\u0441\u043A\u0430\u0442\u044C') or contains(@placeholder,'\u041D\u0430\u0439\u0442\u0438')]"
    );
    private static final By SEARCH_SUBMIT = By.xpath(
            "//button[@data-auto='search-submit']" +
            " | //form[contains(@action,'search')]//button[@type='submit']" +
            " | //button[.//*[name()='svg'] and (contains(@aria-label,'\u041D\u0430\u0439\u0442\u0438') or contains(@title,'\u041D\u0430\u0439\u0442\u0438'))]"
    );
    private static final By REGION_CONFIRM = By.xpath(
            "//button[contains(normalize-space(.),'\u0412\u0441\u0451 \u0432\u0435\u0440\u043D\u043E')]" +
            " | //button[contains(normalize-space(.),'\u0412\u0441\u0435 \u0432\u0435\u0440\u043D\u043E')]" +
            " | //button[contains(normalize-space(.),'\u0425\u043E\u0440\u043E\u0448\u043E')]" +
            " | //button[normalize-space(.)='\u041E\u041A']"
    );
    private static final By COOKIE_ACCEPT = By.xpath(
            "//button[contains(normalize-space(.),'\u041F\u0440\u0438\u043D\u044F\u0442\u044C')]" +
            " | //button[contains(normalize-space(.),'\u041F\u043E\u043D\u044F\u0442\u043D\u043E')]"
    );
    private static final By LOGIN_BUTTON = By.xpath(
            "//a[contains(@href,'passport.yandex')]" +
            " | //button[contains(normalize-space(.),'\u0412\u043E\u0439\u0442\u0438')]" +
            " | //a[contains(normalize-space(.),'\u0412\u043E\u0439\u0442\u0438')]" +
            " | //*[@data-auto='login-button']" +
            " | //*[@data-zone-name='login']"
    );

    public MainPage(WebDriver driver) {
        super(driver);
    }

    public MainPage open() {
        driver.get(URL);
        dismissDialogs();
        return this;
    }

    public SearchResultsPage search(String query) {
        WebElement input = waitClickable(SEARCH_INPUT);
        input.clear();
        input.sendKeys(query);

        if (isPresent(SEARCH_SUBMIT)) {
            waitClickable(SEARCH_SUBMIT).click();
        } else {
            input.sendKeys(Keys.ENTER);
        }
        return new SearchResultsPage(driver);
    }

    public AuthPage openLogin() {
        Set<String> oldWindows = driver.getWindowHandles();
        if (isPresent(LOGIN_BUTTON)) {
            waitClickable(LOGIN_BUTTON).click();
            switchToNewWindowIfOpened(oldWindows);
        } else {
            driver.get("https://passport.yandex.ru/auth");
        }
        return new AuthPage(driver);
    }

    public boolean hasSearchInput() {
        return isPresent(SEARCH_INPUT);
    }

    private void dismissDialogs() {
        clickIfPresent(REGION_CONFIRM);
        clickIfPresent(COOKIE_ACCEPT);
    }

    private void switchToNewWindowIfOpened(Set<String> oldWindows) {
        try {
            shortWait.until(driver -> driver.getWindowHandles().size() > oldWindows.size());
            driver.getWindowHandles().stream()
                    .filter(handle -> !oldWindows.contains(handle))
                    .findFirst()
                    .ifPresent(driver.switchTo()::window);
        } catch (Exception ignored) {
        }
    }
}
