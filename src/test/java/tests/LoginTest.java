package tests;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import pages.AuthPage;
import pages.MainPage;

import static org.junit.jupiter.api.Assertions.assertTrue;

class LoginTest extends BaseTest {

    private static final String PHONE = "9115624293"; // +7 уже предзаполнен в поле

    @ParameterizedTest(name = "Успешная авторизация по коду [{0}]")
    @MethodSource("firefoxOnly")
    void loginWithCode(String browser) {
        setupFresh(browser);

        AuthPage auth = new MainPage(driver)
                .open()
                .openLogin()
                .enterPhoneAndSubmit(PHONE);

        System.out.println(">>> Введите push-код если пришёл, или ждите — тест переключится на SMS автоматически...");
        boolean loggedIn = auth.waitForPushOrSmsFlow(300);

        assertTrue(loggedIn, "После ввода кода должна произойти успешная авторизация");
    }
}
