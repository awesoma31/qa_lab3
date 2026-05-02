package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class CartPage extends BasePage {

    private static final By CART_ITEM = By.xpath(
            "//*[@data-auto='cart-item']" +
            " | //article[contains(@data-auto,'cart')]" +
            " | //div[contains(@class,'cart') and .//button]"
    );
    private static final By CHECKOUT_BUTTON = By.xpath(
            "//a[contains(@href,'checkout')]" +
            " | //button[contains(normalize-space(.),'Оформить')]" +
            " | //a[contains(normalize-space(.),'Оформить')]"
    );
    private static final By EMPTY_CART = By.xpath(
            "//*[contains(normalize-space(.),'Корзина пуста')]" +
            " | //*[contains(normalize-space(.),'В корзине пока пусто')]"
    );

    public CartPage(WebDriver driver) {
        super(driver);
    }

    public boolean hasItems() {
        return isPresent(CART_ITEM);
    }

    public boolean hasCheckoutButton() {
        return isPresent(CHECKOUT_BUTTON);
    }

    public boolean isEmpty() {
        return isPresent(EMPTY_CART);
    }
}
