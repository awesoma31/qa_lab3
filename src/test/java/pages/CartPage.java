package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class CartPage extends BasePage {

    private static final By CART_ITEM = By.xpath(
            "//*[@data-auto='cart-item']" +
            " | //*[@data-auto='cartItem']" +
            " | //article[contains(@data-auto,'cart')]" +
            " | //*[contains(@data-auto,'CartItem')]"
    );
    private static final By CHECKOUT_BUTTON = By.xpath(
            "//a[contains(@href,'checkout')]" +
            " | //button[contains(normalize-space(.),'Оформить')]" +
            " | //a[contains(normalize-space(.),'Оформить')]" +
            " | //button[contains(normalize-space(.),'к оформлению')]" +
            " | //a[contains(normalize-space(.),'к оформлению')]" +
            " | //button[contains(normalize-space(.),'К оформлению')]" +
            " | //a[contains(normalize-space(.),'К оформлению')]"
    );
    private static final By EMPTY_CART = By.xpath(
            "//*[contains(normalize-space(.),'Корзина пуста')]" +
            " | //*[contains(normalize-space(.),'В корзине пока пусто')]" +
            " | //*[contains(normalize-space(.),'пусто')]"
    );

    public CartPage(WebDriver driver) {
        super(driver);
    }

    public boolean hasItems() {
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(CART_ITEM));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean hasCheckoutButton() {
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(CHECKOUT_BUTTON));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isOnAuthPage() {
        return driver.getCurrentUrl().contains("passport.yandex");
    }

    public boolean isEmpty() {
        return isPresent(EMPTY_CART);
    }
}
