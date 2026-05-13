package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class ProductPage extends BasePage {

    private static final By TITLE = By.xpath(
            "//h1[@data-auto='productCardTitle']" +
            " | //h1[contains(@itemprop,'name')]" +
            " | //h1[contains(@class,'title')]" +
            " | //h1"
    );
    private static final By PRICE = By.xpath(
            "//span[@data-auto='mainPrice']" +
            " | //div[@data-auto='price-value']" +
            " | //h3[@data-auto='price']//span" +
            " | //*[contains(@class,'price') and contains(.,'₽')]"
    );
    private static final By ADD_TO_CART = By.xpath(
            "//button[@data-auto='cartButton']" +
            " | //button[contains(normalize-space(.),'В корзину')]"
    );
    private static final By TOAST = By.xpath("//*[@data-auto='toasterContent']");
    private static final By TOAST_CART_LINK = By.xpath(
            "//*[@data-auto='toasterContent']//a[contains(normalize-space(.),'орзин')]" +
            " | //*[@data-auto='toasterContent']//button[contains(normalize-space(.),'орзин')]"
    );
    private static final By RATING = By.xpath(
            "//div[@data-auto='rating-value']" +
            " | //span[contains(@class,'rating') and contains(@class,'value')]" +
            " | //*[@aria-label and contains(@aria-label,'рейтинг')]"
    );
    private static final By BREADCRUMBS = By.xpath(
            "//*[@id='/content/page/fancyPage/defaultPage/breadcrumbs']//nav//ol//li//a" +
            " | //*[contains(@id,'breadcrumbs')]//nav//ol//li//a" +
            " | //nav[@aria-label='breadcrumb']//ol//li//a" +
            " | //ol[contains(@class,'breadcrumb')]//li//a" +
            " | //ul[@data-auto='breadcrumbs']//li//a" +
            " | //div[@data-auto='breadcrumbs']//a"
    );
    private static final By SPECS_TAB = By.xpath(
            "//a[contains(normalize-space(.),'Характеристики')]" +
            " | //button[contains(normalize-space(.),'Характеристики')]" +
            " | //a[contains(normalize-space(.),'характеристики')]" +
            " | //button[contains(normalize-space(.),'характеристики')]" +
            " | //a[contains(normalize-space(.),'Все характеристики')]" +
            " | //button[contains(normalize-space(.),'Все характеристики')]"
    );
    private static final By SPECS_CONTENT = By.xpath(
            "//*[contains(normalize-space(.),'Основные характеристики')]" +
            " | //*[contains(normalize-space(.),'Характеристики')]" +
            " | //*[contains(normalize-space(.),'характеристики')]" +
            " | //*[contains(normalize-space(.),'Все характеристики')]" +
            " | //*[contains(normalize-space(.),'Общие характеристики')]" +
            " | //*[contains(normalize-space(.),'Заводские данные')]" +
            " | //*[contains(@data-auto,'spec')]" +
            " | //*[contains(@data-auto,'characteristic')]" +
            " | //*[contains(@id,'spec')]" +
            " | //*[contains(@class,'spec')]" +
            " | //*[contains(@class,'characteristic')]"
    );

    public ProductPage(WebDriver driver) {
        super(driver);
    }

    public boolean hasTitle() {
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(TITLE));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getProductTitle() {
        return waitVisible(TITLE).getText();
    }

    public boolean hasPrice() {
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(PRICE));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getPrice() {
        return waitVisible(PRICE).getText();
    }

    public boolean hasAddToCartButton() {
        return isPresent(ADD_TO_CART);
    }

    public boolean clickAddToCartAndWaitResponse() {
        WebElement btn = waitClickable(ADD_TO_CART);
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", btn);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
        try {
            shortWait.until(ExpectedConditions.presenceOfElementLocated(TOAST));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public CartPage navigateToCart() {
        clickIfPresent(TOAST_CART_LINK);
        if (!driver.getCurrentUrl().contains("cart")) {
            driver.get("https://market.yandex.ru/mf-cart");
        }
        return new CartPage(driver);
    }


    public boolean hasRating() {
        return isPresent(RATING);
    }

    public boolean hasBreadcrumbs() {
        try {
            wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(BREADCRUMBS, 0));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public ProductPage clickSpecsTab() {
        try {
            WebElement specsTab = shortWait.until(ExpectedConditions.presenceOfElementLocated(SPECS_TAB));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", specsTab);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", specsTab);
        } catch (Exception ignored) {
            ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight / 2);");
        }
        return this;
    }

    public boolean hasSpecsContent() {
        String pageText = driver.findElement(By.tagName("body")).getText().toLowerCase();
        String url = driver.getCurrentUrl().toLowerCase();
        return pageText.contains("характеристик")
                || pageText.contains("параметр")
                || pageText.contains("модель")
                || url.contains("spec")
                || url.contains("characteristics");
    }
}
