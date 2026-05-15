package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.List;

public class SearchResultsPage extends BasePage {

    private static final By PRODUCT_CARDS = By.xpath(
            "//article[@data-auto='productCard']" +
            " | //div[@data-auto='snippet-cell']" +
            " | //li[contains(@data-auto,'productCard')]" +
            " | //*[@data-auto='snippet-title']"
    );
    private static final By PRODUCT_TITLES = By.xpath(
            "//h3[@data-auto='snippet-title']//span" +
            " | //a[@data-auto='snippet-title-link']" +
            " | //div[@data-auto='snippet-title']//span" +
            " | //*[@data-auto='snippet-title']" +
            " | //article[@data-auto='productCard']//h3" +
            " | //div[@data-auto='snippet-cell']//h3" +
            " | //li[contains(@data-auto,'productCard')]//h3"
    );
    private static final By FIRST_PRODUCT_LINK = By.xpath(
            "(//*[@data-auto='snippet-title'] | //a[@data-auto='snippet-title-link'])[1]"
    );
    private static final By NO_RESULTS = By.xpath(
            "//*[contains(normalize-space(.),'Ничего не нашлось')" +
            " or contains(normalize-space(.),'Ничего не найдено')" +
            " or contains(normalize-space(.),'не найден')]"
    );
    private static final By PAGE_ERROR = By.xpath(
            "//*[contains(normalize-space(.),'что-то пошло не так')" +
            " or contains(normalize-space(.),'Что-то пошло не так')" +
            " or contains(normalize-space(.),'что то пошло не так')" +
            " or contains(normalize-space(.),'Произошла ошибка')" +
            " or contains(normalize-space(.),'произошла ошибка')" +
            " or //*[@data-auto='error-page']" +
            " or //*[contains(@class,'error-page')]" +
            " or //*[contains(@class,'ErrorPage')]" +
            "]"
    );
    private static final By PRICES = By.xpath(
            "//span[@data-auto='mainPrice']" +
            " | //*[@data-auto='snippet-price-current']" +
            " | //div[@data-auto='price']//span[contains(.,'₽')]" +
            " | //span[contains(@class,'price') and contains(.,'₽')]" +
            " | //*[contains(@data-auto,'price') and contains(.,'₽')]"
    );
    private static final By SORT_TRIGGER = By.xpath(
            "//button[contains(@aria-label,'Показать сначала')]" +
            " | //button[contains(normalize-space(.),'Сначала')]"
    );
    private static final By SORT_BY_PRICE_DESC = By.xpath(
            "//*[self::button or self::a or @role='option' or @role='menuitem']" +
            "[contains(translate(normalize-space(.)," +
            "'АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ'," +
            "'абвгдеёжзийклмнопрстуфхцчшщъыьэюя'),'подороже')" +
            " or contains(translate(normalize-space(.)," +
            "'АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ'," +
            "'абвгдеёжзийклмнопрстуфхцчшщъыьэюя'),'дорог')" +
            " or contains(translate(normalize-space(.)," +
            "'АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ'," +
            "'абвгдеёжзийклмнопрстуфхцчшщъыьэюя'),'убыв')]"
    );
    private static final By DELIVERY_FILTER = By.xpath(
            "//label[contains(normalize-space(.),'Доставка')]" +
            " | //button[contains(normalize-space(.),'Доставка')]"
    );
    private static final By PRICE_FROM_INPUT = By.xpath(
            "//input[@id='glpricefrom']" +
            " | //input[contains(@id,'pricefrom') or contains(@name,'pricefrom')]" +
            " | //input[@data-auto='filter-range-glprice-1']" +
            " | //div[contains(@data-auto,'filter-price') or contains(@data-auto,'glprice')]//input[1]"
    );
    private static final By PRICE_TO_INPUT = By.xpath(
            "//input[@id='glpriceto']" +
            " | //input[contains(@id,'priceto') or contains(@name,'priceto')]" +
            " | //input[@data-auto='filter-range-glprice-2']" +
            " | //div[contains(@data-auto,'filter-price') or contains(@data-auto,'glprice')]//input[2]"
    );

    public SearchResultsPage(WebDriver driver) {
        super(driver);
    }

    public boolean waitForResults() {
        try {
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.presenceOfElementLocated(PRODUCT_CARDS),
                    ExpectedConditions.presenceOfElementLocated(NO_RESULTS)
            ));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean hasNoResultsOrError() {
        try {
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.presenceOfElementLocated(NO_RESULTS),
                    ExpectedConditions.presenceOfElementLocated(PAGE_ERROR)
            ));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean hasResults() {
        return !findAll(PRODUCT_CARDS).isEmpty();
    }

    public boolean hasNoResultsMessage() {
        return isPresent(NO_RESULTS);
    }

    public int getResultCount() {
        return findAll(PRODUCT_CARDS).size();
    }

    public List<String> getProductTitles() {
        List<String> titles = findAll(PRODUCT_TITLES).stream()
                .map(WebElement::getText)
                .map(String::trim)
                .filter(t -> !t.isBlank())
                .toList();
        if (!titles.isEmpty()) {
            return titles;
        }
        return findAll(PRODUCT_CARDS).stream()
                .map(WebElement::getText)
                .map(t -> t.lines().findFirst().orElse("").trim())
                .filter(t -> !t.isBlank())
                .toList();
    }

    public List<String> getPrices() {
        try {
            return wait.until(driver -> {
                try {
                    List<String> prices = driver.findElements(PRICES).stream()
                            .map(WebElement::getText)
                            .map(String::trim)
                            .filter(t -> !t.isBlank())
                            .toList();
                    return prices.isEmpty() ? null : prices;
                } catch (StaleElementReferenceException e) {
                    return null;
                }
            });
        } catch (Exception e) {
            return List.of();
        }
    }

    public ProductPage openFirstProduct() {
        Set<String> oldWindows = driver.getWindowHandles();
        waitClickable(FIRST_PRODUCT_LINK).click();
        wait.until(driver -> driver.getWindowHandles().size() > oldWindows.size());
        driver.getWindowHandles().stream()
                .filter(handle -> !oldWindows.contains(handle))
                .findFirst()
                .ifPresent(driver.switchTo()::window);
        return new ProductPage(driver);
    }

    public boolean hasSortByPriceButton() {
        return isPresent(SORT_TRIGGER);
    }

    public SearchResultsPage sortByPriceDescending() {
        waitClickable(SORT_TRIGGER).click();
        try {
            WebElement option = shortWait.until(ExpectedConditions.presenceOfElementLocated(SORT_BY_PRICE_DESC));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", option);
        } catch (Exception e) {
            driver.get(priceDescendingUrl());
        }
        waitForResults();
        return this;
    }

    private String priceDescendingUrl() {
        String url = getUrl();
        if (url.matches(".*[?&]how=[^&]*.*")) {
            return url.replaceAll("([?&])how=[^&]*", "$1how=dprice");
        }
        return url + (url.contains("?") ? "&" : "?") + "how=dprice";
    }

    public boolean hasPriceFilter() {
        return isPresent(PRICE_FROM_INPUT);
    }

    public SearchResultsPage setPriceRange(int from, int to) {
        driver.get(priceRangeUrl(from, to));
        sleep(1);
        waitForResults();
        sleep(1);
        try {
            wait.until(d -> {
                try {
                    return !d.findElements(PRICES).isEmpty();
                } catch (StaleElementReferenceException e) {
                    return false;
                }
            });
        } catch (Exception ignored) {}
        sleep(1);
        return this;
    }

    private void sleep(int seconds) {
        try { Thread.sleep(seconds * 1_000L); } catch (InterruptedException ignored) {}
    }

    private String priceRangeUrl(int from, int to) {
        String url = getUrl();
        String params = "pricefrom=" + from + (to > 0 ? "&priceto=" + to : "");
        return url + (url.contains("?") ? "&" : "?") + params;
    }

    public boolean hasDeliveryFilter() {
        return isPresent(DELIVERY_FILTER);
    }

    public String decodedUrl() {
        return URLDecoder.decode(getUrl(), StandardCharsets.UTF_8);
    }

    public List<Integer> getParsedPrices() {
        return getPrices().stream()
                .map(p -> p.replaceAll("[^0-9]", ""))
                .filter(p -> !p.isEmpty())
                .map(Integer::parseInt)
                .toList();
    }
}
