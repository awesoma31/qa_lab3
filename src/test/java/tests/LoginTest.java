package tests;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import pages.AuthPage;
import pages.MainPage;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Тест успешной авторизации по SMS.
 * Запускать отдельно: mvn test -Dtest=LoginTest
 * Во время теста необходимо вручную ввести код из SMS в открывшемся браузере.
 */
class LoginTest extends BaseTest {

    private static final String PHONE = "9115624293"; // +7 уже предзаполнен в поле

    @ParameterizedTest(name = "Успешная авторизация по SMS [{0}]")
    @MethodSource("firefoxOnly")
    void loginWithSmsCode(String browser) {
        setupFresh(browser);

        AuthPage auth = new MainPage(driver)
                .open()
                .openLogin()
                .enterPhoneAndSubmit(PHONE);

        // Яндекс сначала предлагает код из приложения — ждём кнопку "по SMS" до 90 сек
        auth.waitForSmsButtonAndClick();

        // Ждём ручного ввода кода пользователем — до 5 минут
        System.out.println(">>> Введите SMS-код в браузере. Ожидание до 5 минут...");
        boolean loggedIn = auth.waitForManualLogin(300);

        assertTrue(loggedIn, "После ввода SMS-кода должна произойти успешная авторизация");
    }
}
