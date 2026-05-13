package tests;

import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import pages.CartPage;
import pages.MainPage;
import pages.ProductPage;
import pages.SearchResultsPage;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@Execution(ExecutionMode.CONCURRENT)
class SearchTest extends BaseTest {

    @ParameterizedTest(name = "TC-01 Главная страница содержит поиск [{0}]")
    @MethodSource("browsers")
    void mainPageHasSearch(String browser) {
        setup(browser);

        MainPage mainPage = new MainPage(driver).open();

        assertTrue(mainPage.hasSearchInput(), "На главной странице должно быть поле поиска");
    }

    @ParameterizedTest(name = "TC-02 Поиск возвращает товары [{0}]")
    @MethodSource("browsers")
    void searchReturnsResults(String browser) {
        setup(browser);

        SearchResultsPage results = new MainPage(driver).open().search("ноутбук");
        results.waitForResults();

        assertTrue(results.hasResults(), "Поиск по запросу 'ноутбук' должен вернуть карточки товаров");
    }

    @ParameterizedTest(name = "TC-03 Запрос отражается в URL [{0}]")
    @MethodSource("browsers")
    void searchQueryAppearsInUrl(String browser) {
        setup(browser);

        SearchResultsPage results = new MainPage(driver).open().search("смартфон");
        results.waitForResults();

        assertTrue(results.decodedUrl().contains("смартфон"),
                "URL страницы результатов должен содержать поисковый запрос. URL: " + results.getUrl());
    }

    @ParameterizedTest(name = "TC-04 Названия найденных товаров соответствуют запросу [{0}]")
    @MethodSource("browsers")
    void resultTitlesContainQueryWord(String browser) {
        setup(browser);

        SearchResultsPage results = new MainPage(driver).open().search("телевизор");
        results.waitForResults();
        List<String> titles = results.getProductTitles();

        assertFalse(titles.isEmpty(), "В результатах поиска должны быть названия товаров");
        assertTrue(titles.stream().anyMatch(title -> title.toLowerCase().contains("телевизор")),
                "Хотя бы один результат должен содержать слово 'телевизор'. Найдено: " + titles);
    }

    @ParameterizedTest(name = "TC-05 На странице результатов доступны цены[{0}]")
    @MethodSource("browsers")
    void resultPageHasPricesAndDeliveryFilter(String browser) {
        setup(browser);

        SearchResultsPage results = new MainPage(driver).open().search("наушники");
        results.waitForResults();

        assertFalse(results.getPrices().isEmpty(), "В результатах поиска должны отображаться цены");
    }

    @ParameterizedTest(name = "TC-06 Страница товара содержит заголовок и цену [{0}]")
    @MethodSource("browsers")
    void productPageHasTitleAndPrice(String browser) {
        setup(browser);

        ProductPage product = new MainPage(driver)
                .open()
                .search("телевизор")
                .openFirstProduct();

        assertTrue(product.hasTitle(), "Страница товара должна содержать заголовок");
        assertTrue(product.hasPrice(), "Страница товара должна содержать цену");
    }

    @ParameterizedTest(name = "TC-07 Страница товара содержит навигацию и характеристики [{0}]")
    @MethodSource("browsers")
    void productPageHasBreadcrumbsAndSpecs(String browser) {
        setup(browser);

        ProductPage product = new MainPage(driver)
                .open()
                .search("холодильник")
                .openFirstProduct();

        assertTrue(product.hasBreadcrumbs(), "Страница товара должна содержать хлебные крошки");
        assertTrue(product.clickSpecsTab().hasSpecsContent(),
                "На странице товара должен быть блок или вкладка характеристик");
    }

//    @ParameterizedTest(name = "TC-08 Можно добавить товар в корзину и перейти к оформлению [{0}]")
//    @MethodSource("browsers")
//    void productCanBeAddedToCart() {
//        setup("firefox");
//
//        ProductPage product = new MainPage(driver)
//                .open()
//                .search("мышь компьютерная")
//                .openFirstProduct();
//
//        assumeTrue(product.hasAddToCartButton(),
//                "Для выбранного товара нет кнопки добавления в корзину, сценарий не применим");
//        CartPage cart = product.addToCart();
//
//        assertTrue(cart.hasItems() || !cart.isEmpty(), "После добавления товара корзина не должна быть пустой");
//        assertTrue(cart.hasCheckoutButton(), "В корзине должна быть доступна кнопка оформления заказа");
//    }

    @ParameterizedTest(name = "TC-09 Сортировка по цене доступна и возвращает цены [{0}]")
    @MethodSource("browsers")
    void sortingByPriceDescendingReturnsPrices(String browser) {
        setup(browser);

        SearchResultsPage results = new MainPage(driver).open().search("пылесос");
        results.waitForResults();
        assumeTrue(results.hasSortByPriceButton(), "На странице не найден элемент сортировки по цене");

        results.sortByPriceDescending();

        assertFalse(results.getParsedPrices().isEmpty(),
                "После сортировки должны отображаться товары с распознанными ценами");
    }
}
