package driver;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

public class DriverFactory {

    private static final int SCREEN_WIDTH = 1920;
    private static final int SCREEN_HEIGHT = 1080;

    private static final boolean HEADLESS =
            Boolean.parseBoolean(System.getProperty("headless", "false"));

    private static final File PROFILES_DIR =
            new File(System.getProperty("profiles.dir", "profiles")).getAbsoluteFile();

    public static WebDriver create(String browser) {
        return create(browser, true);
    }

    public static WebDriver create(String browser, boolean useProfile) {
        WebDriver driver = switch (browser.toLowerCase()) {
            case "chrome" -> createChrome(useProfile);
            case "firefox" -> createFirefox(useProfile);
            default -> throw new IllegalArgumentException("Unsupported browser: " + browser);
        };
        driver.manage().window().setPosition(new Point(0, 0));
        driver.manage().window().setSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        return driver;
    }

    private static WebDriver createChrome(boolean useProfile) {
        ChromeOptions opts = new ChromeOptions();
        if (HEADLESS) {
            opts.addArguments("--headless=new");
        }
        opts.addArguments(
                "--no-sandbox",
                "--disable-dev-shm-usage",
                "--window-size=" + SCREEN_WIDTH + "," + SCREEN_HEIGHT,
                "--disable-blink-features=AutomationControlled",
                "--user-agent=Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"
        );
        opts.setExperimentalOption("excludeSwitches", new java.util.ArrayList<>(List.of("enable-automation")));
        opts.setExperimentalOption("useAutomationExtension", false);

        File chromeProfile = new File(PROFILES_DIR, "chrome");
        if (useProfile && chromeProfile.exists()) {
            try {
                // Chrome блокирует user-data-dir — копируем профиль во временную папку
                Path tempDir = copyToTemp(chromeProfile.toPath());
                opts.addArguments(
                        "--user-data-dir=" + tempDir.toAbsolutePath(),
                        "--profile-directory=Default"
                );
            } catch (IOException ignored) {}
        }

        return new ChromeDriver(opts);
    }

    private static WebDriver createFirefox(boolean useProfile) {
        FirefoxOptions opts = new FirefoxOptions();
        if (HEADLESS) {
            opts.addArguments("-headless");
        }
        opts.addArguments("-width=" + SCREEN_WIDTH, "-height=" + SCREEN_HEIGHT);

        File ffProfile = new File(PROFILES_DIR, "firefox");
        if (useProfile && ffProfile.exists()) {
            opts.setProfile(new FirefoxProfile(ffProfile));
        }

        return new FirefoxDriver(opts);
    }

    private static Path copyToTemp(Path source) throws IOException {
        Path tempDir = Files.createTempDirectory("chromium-profile-");
        Files.walkFileTree(source, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Files.createDirectories(tempDir.resolve(source.relativize(dir)));
                return FileVisitResult.CONTINUE;
            }
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.copy(file, tempDir.resolve(source.relativize(file)), StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }
            @Override
            public FileVisitResult visitFileFailed(Path file, IOException e) {
                return FileVisitResult.CONTINUE;
            }
        });
        return tempDir;
    }
}
