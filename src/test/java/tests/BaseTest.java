package tests;

import driver.DriverFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.provider.Arguments;
import org.openqa.selenium.WebDriver;

import java.util.stream.Stream;

public abstract class BaseTest {

    protected WebDriver driver;

    protected static Stream<Arguments> browsers() {
        return Stream.of(
                Arguments.of("chrome"),
                Arguments.of("firefox")
        );
    }

    protected static Stream<Arguments> firefoxOnly() {
        return Stream.of(Arguments.of("firefox"));
    }

    protected void setup(String browser) {
        driver = DriverFactory.create(browser, false);
    }

    protected void setupFresh(String browser) {
        driver = DriverFactory.create(browser, false);
    }

    protected void setupWithProfile(String browser) {
        driver = DriverFactory.create(browser, true);
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }
}
