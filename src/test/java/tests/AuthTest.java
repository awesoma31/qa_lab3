package tests;

import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import pages.AuthPage;
import pages.MainPage;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Execution(ExecutionMode.CONCURRENT)
class AuthTest extends BaseTest {

    private static final String EXISTING_EMAIL =
            System.getProperty("auth.email", "i.nikitc@yandex.ru");
    private static final String WRONG_PASSWORD = "WrongPassword123!";
    private static final String UNKNOWN_USERNAME = "unknown-user-tpo-lab3-404";
    private static final String EXISTING_PHONE =
            System.getProperty("auth.phone", "9509579573");
    private static final String INVALID_PHONE = "123";


    //TODO: иногда всплывает окно для кого хотите создать аккаунт. надо пофиксить
    @ParameterizedTest(name = "Registration with existing phone [{0}]")
    @MethodSource("browsers")
    void registrationWithExistingPhoneShowsError(String browser) {
        setup(browser);

        AuthPage authPage = new MainPage(driver)
                .open()
                .openLogin()
                .openRegistrationFromLogin()
                .registerByPhone(EXISTING_PHONE);

        assertTrue(authPage.hasValidationErrorOrSmsChallenge(),
                "Registration with an existing phone should show a validation error or confirmation challenge");
    }

    @ParameterizedTest(name = "Registration with invalid phone [{0}]")
    @MethodSource("browsers")
    void registrationWithInvalidPhoneShowsError(String browser) {
        setup(browser);

        AuthPage authPage = new MainPage(driver)
                .open()
                .openLogin()
                .openRegistrationFromLogin()
                .registerByPhone(INVALID_PHONE);

        assertTrue(authPage.hasValidationError(),
                "Registration with an invalid phone should show a validation error");
    }

    @ParameterizedTest(name = "Registration with empty fields [{0}]")
    @MethodSource("browsers")
    void registrationWithEmptyFieldsShowsError(String browser) {
        setup(browser);

        AuthPage authPage = new MainPage(driver)
                .open()
                .openLogin()
                .openRegistrationFromLogin()
                .submitEmptyRegistration();

        assertTrue(authPage.hasValidationError(),
                "Registration with empty fields should show validation errors");
    }

    @ParameterizedTest(name = "Login with unknown username [{0}]")
    @MethodSource("browsers")
    void loginWithUnknownUsernameShowsError(String browser) {
        setup(browser);

        AuthPage authPage = new MainPage(driver)
                .open()
                .openLogin()
                .openLoginByUsername()
                .enterUsername(UNKNOWN_USERNAME);

        assertTrue(authPage.hasValidationError(),
                "Login with an unknown username should show a validation error");
    }

    @ParameterizedTest(name = "Login with existing username and wrong password [{0}]")
    @MethodSource("browsers")
    void loginWithExistingUsernameAndWrongPasswordShowsError(String browser) {
        setup(browser);

        AuthPage authPage = new MainPage(driver)
                .open()
                .openLogin()
                .openLoginByUsername()
                .enterUsername(EXISTING_EMAIL)
                .enterPassword(WRONG_PASSWORD);

        assertTrue(authPage.hasValidationError(),
                "Login with a wrong password should show a validation error");
    }

    @ParameterizedTest(name = "Login with empty fields [{0}]")
    @MethodSource("browsers")
    void loginWithEmptyFieldsShowsError(String browser) {
        setup(browser);

        AuthPage authPage = new MainPage(driver)
                .open()
                .openLogin()
                .submitEmptyLogin();

        assertTrue(authPage.hasValidationError(),
                "Login with empty fields should show validation errors");
    }
}
