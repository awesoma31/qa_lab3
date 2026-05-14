# TPO Lab 3 — Функциональное тестирование Яндекс Маркета

## Цель и вариант

**Вариант 1771:** Поиск, выбор и покупка товаров на маркетплейсе [market.yandex.ru](https://market.yandex.ru).

Задача — написать автоматические UI-тесты с помощью Selenium WebDriver, покрывающие основные пользовательские сценарии: поиск товара, просмотр результатов, открытие страницы товара, добавление в корзину, авторизацию.

---

## Как работает Selenium WebDriver

Selenium — это библиотека для управления браузером из кода. Архитектура работает по следующей схеме:

```
Java-код (тест)
    ↓  HTTP (W3C WebDriver Protocol)
WebDriver-сервер (chromedriver / geckodriver)
    ↓  DevTools / internal API
Браузер (Chrome / Firefox)
    ↓  HTTP
Сайт (market.yandex.ru)
```

1. Тест создаёт объект `WebDriver` — он запускает процесс `chromedriver` или `geckodriver`.
2. Драйвер поднимает локальный HTTP-сервер и открывает браузер.
3. Каждый вызов (`.findElement()`, `.click()`, `.sendKeys()`) — это HTTP-запрос к драйверу по протоколу W3C WebDriver.
4. Драйвер транслирует команды в браузер и возвращает результат.
5. После теста `driver.quit()` завершает сессию и закрывает браузер.

**Важный нюанс:** браузер, запущенный через WebDriver, автоматически выставляет флаг `navigator.webdriver = true`. Яндекс считывает этот флаг и включает ботозащиту (блокирует корзину, прячет цены). В коде это частично обходится через `--disable-blink-features=AutomationControlled` и кастомный `user-agent`.

---

## Структура проекта

```
lab3/
├── pom.xml                          # Maven: зависимости и плагины
├── profiles/                        # Папки с браузерными профилями (не в git)
│   ├── chrome/                      # Скопированный профиль Chrome
│   └── firefox/                     # Профиль Firefox
├── src/test/
│   ├── java/
│   │   ├── driver/
│   │   │   └── DriverFactory.java   # Фабрика WebDriver
│   │   ├── pages/
│   │   │   ├── BasePage.java        # Базовый класс Page Object
│   │   │   ├── MainPage.java        # Главная страница Яндекс Маркета
│   │   │   ├── SearchResultsPage.java # Страница результатов поиска
│   │   │   ├── ProductPage.java     # Страница карточки товара
│   │   │   ├── CartPage.java        # Корзина
│   │   │   └── AuthPage.java        # Страница авторизации/регистрации
│   │   └── tests/
│   │       ├── BaseTest.java        # Базовый класс тестов
│   │       ├── SearchTest.java      # TC-01..TC-09: поиск и покупка
│   │       ├── AuthTest.java        # Негативные тесты регистрации
│   │       └── LoginTest.java       # Тест успешного входа по SMS
│   └── resources/
│       └── junit-platform.properties # Настройки параллельного запуска
└── use-cases.md                     # Mermaid-диаграммы вариантов использования
```

---

## Архитектурный паттерн: Page Object Model

Каждая страница сайта представлена отдельным Java-классом. Это разделяет **"что делать"** (логика теста) от **"как найти элемент"** (XPath-локаторы). Если Яндекс изменит вёрстку, достаточно поправить локатор в одном классе, не трогая тесты.

```
тест (SearchTest)
    ↓ вызывает методы
Page Object (SearchResultsPage)
    ↓ использует локаторы (By.xpath)
Selenium API
    ↓
Браузер
```

### Цепочка вызовов в тесте

```java
new MainPage(driver)
    .open()                     // driver.get("https://market.yandex.ru")
    .search("телевизор")        // заполняет поле и нажимает Enter → возвращает SearchResultsPage
    .openFirstProduct()         // кликает по первому результату → возвращает ProductPage
    .clickSpecsTab()            // прокручивает и кликает по вкладке
    .hasSpecsContent()          // читает текст страницы → boolean
```

Каждый метод возвращает объект нужной страницы — это **fluent API** / метод-цепочка.

---

## Слой 1: BasePage

`src/test/java/pages/BasePage.java`

Базовый класс, от которого наследуют все Page Objects. Содержит:

- `WebDriver driver` — ссылка на браузер
- `WebDriverWait wait` — ожидание до 30 секунд (вместо `Thread.sleep`)
- `WebDriverWait shortWait` — ожидание до 5 секунд
- Вспомогательные методы:

| Метод | Что делает |
|-------|-----------|
| `waitVisible(By)` | Ждёт появления элемента на экране |
| `waitClickable(By)` | Ждёт, пока элемент не станет кликабельным |
| `isPresent(By)` | Проверяет наличие элемента без исключения (shortWait) |
| `clickIfPresent(By)` | Кликает, если элемент найден, иначе возвращает false |
| `findAll(By)` | Возвращает список всех совпадающих элементов |

**Почему нет `Thread.sleep()`?** Жёсткая пауза — антипаттерн: тест либо ждёт слишком долго (медленный), либо слишком мало (ненадёжный). `WebDriverWait` опрашивает условие каждые 500 мс и продолжает сразу, как условие выполнено.

---

## Слой 2: Page Objects

### MainPage

Главная страница `market.yandex.ru`.

Локаторы используют `data-auto` атрибуты — специальные атрибуты, которые Яндекс ставит для автоматизации. Они стабильнее, чем CSS-классы (которые минифицируются) или положение в DOM.

```java
// Пример: поле поиска находится по нескольким вариантам XPath сразу
private static final By SEARCH_INPUT = By.xpath(
    "//input[@data-auto='search-input-field']"
    + " | //form[contains(@action,'search')]//input[@type='text' or @type='search']"
    + " | //input[contains(@placeholder,'Искать') or contains(@placeholder,'Найти')]"
);
```

Оператор `|` в XPath — это объединение: если первый вариант не найден, ищется второй и т.д. Это делает локаторы устойчивыми к изменениям вёрстки.

**Метод `open()`** дополнительно закрывает всплывающие диалоги (выбор региона, cookie-banner) через `clickIfPresent`.

**Метод `openLogin()`** обрабатывает случай, когда кнопка входа открывает новую вкладку — переключается на неё через `driver.switchTo().window(handle)`.

### SearchResultsPage

Страница выдачи поиска.

**`waitForResults()`** ждёт появления карточек товаров ИЛИ сообщения "Ничего не нашлось" — оба варианта считаются нормальной загрузкой страницы.

**`getPrices()`** возвращает список цен как строк. Использует внутренний wait с обработкой `StaleElementReferenceException` — это исключение возникает, когда DOM перестраивается (React/SPA-рендеринг) и уже найденный элемент исчезает из дерева.

**`openFirstProduct()`** открывает первый товар и обрабатывает новую вкладку — Яндекс Маркет открывает карточки товара в новом окне браузера.

**`sortByPriceDescending()`** сначала пробует кликнуть по выпадающему меню сортировки, а если не получается — добавляет `?how=dprice` в URL напрямую (fallback).

**`getParsedPrices()`** очищает цены от символов (`₽`, пробелы, неразрывные пробелы) и конвертирует в `List<Integer>`.

### ProductPage

Карточка конкретного товара.

**`clickAddToCartAndWaitResponse()`** — ключевой метод для TC-08:
1. Прокручивает кнопку в область видимости через JS (`scrollIntoView`)
2. Кликает через `JavascriptExecutor` (обходит перекрытие другими элементами)
3. Ждёт тост-уведомление (`data-auto='toasterContent'`) до 5 секунд
4. Возвращает `true` если тост появился (сервер ответил на запрос корзины)

**`navigateToCart()`** сначала пробует кликнуть по ссылке в тосте, иначе переходит напрямую на `https://market.yandex.ru/mf-cart`.

**`hasSpecsContent()`** читает весь текст страницы (`body.getText()`) и проверяет наличие слов типа "характеристик", "параметр", "модель" — это надёжнее, чем искать конкретный элемент, так как вёрстка характеристик сильно варьируется между категориями.

### CartPage

**`isOnAuthPage()`** проверяет URL — если содержит `passport.yandex`, значит пользователь не авторизован и корзина перенаправила на вход.

### AuthPage

Страница Яндекс Паспорта. Самая сложная страница проекта из-за:
- Динамических `react-aria`-идентификаторов (меняются при каждой загрузке)
- Многошагового flow (телефон → SMS-код → подтверждение)
- Двух URL: `passport.yandex.ru/auth` (вход) и `passport.yandex.ru/pwl-yandex/reg` (регистрация)

**`openRegistrationFromLogin()`** — кликает "Ещё" → "Создать ID" через JavaScript (JS-клик обходит проблемы с `pointer-events: none`).

**`typeInto()`** реализует retry (3 попытки), потому что Яндекс Паспорт часто перестраивает форму сразу после загрузки.

**`waitForPushOrSmsFlow(int totalTimeoutSeconds)`** — основной метод для LoginTest, обрабатывает оба сценария входа:
- **Фаза 1 (до 90 с):** ждёт либо ухода URL с `passport.yandex` (пользователь успел ввести push-код), либо появления кнопки "Отправить ещё раз"
- Если URL ушёл — возвращает `true` немедленно (push сработал)
- **Фаза 2:** кликает resend → выбирает "via SMS" из меню → пишет в консоль `">>> SMS-код отправлен..."`
- **Фаза 3 (до totalTimeoutSeconds):** ждёт ухода URL с `passport.yandex` (ручной ввод SMS)

**`waitForSmsButtonAndClick()`** — устаревший вариант: всегда ждёт resend, без обработки push. Оставлен для совместимости.

**`waitForManualLogin(int seconds)`** — ждёт пока URL не покинет `passport.yandex`. Оставлен для совместимости.

---

## Слой 3: DriverFactory

`src/test/java/driver/DriverFactory.java`

Фабрика создаёт и настраивает браузер. Ключевые решения:

### Anti-bot настройки Chrome

```java
opts.addArguments("--disable-blink-features=AutomationControlled");
opts.addArguments("--user-agent=Mozilla/5.0 ...");
opts.setExperimentalOption("excludeSwitches", List.of("enable-automation"));
opts.setExperimentalOption("useAutomationExtension", false);
```

Эти флаги частично маскируют автоматизацию, но `navigator.webdriver` всё равно остаётся `true` в Chromium (браузер это не скрывает при использовании CDP).

### Профили браузера

Профиль — это директория с cookies, историей, настройками. Если пользователь вошёл в Яндекс в браузере с профилем, при следующем запуске будет активная сессия.

**Проблема Chrome:** Chrome блокирует `user-data-dir` — нельзя одновременно запустить два браузера с одной папкой профиля. При параллельных тестах это приводит к краху.

**Решение:** перед запуском теста профиль копируется во временную директорию через `Files.walkFileTree` (Java NIO). Каждый тест получает свою копию.

```java
private static Path copyToTemp(Path source) throws IOException {
    Path tempDir = Files.createTempDirectory("chromium-profile-");
    Files.walkFileTree(source, new SimpleFileVisitor<>() {
        // рекурсивно копирует файлы и папки
    });
    return tempDir;
}
```

**Firefox:** `new FirefoxProfile(File)` сам копирует профиль во временную директорию — проблемы нет.

### Параметр headless

Через системное свойство `headless=true` браузер запускается без GUI (невидимый). Полезно для CI/CD.

```
mvn test -Dheadless=true
```

---

## Слой 4: Тесты

### BaseTest

Базовый класс тестов. Содержит три метода setup:

| Метод | useProfile | Когда использовать |
|-------|-----------|-------------------|
| `setup(browser)` | `false` | Большинство тестов (без сессии) |
| `setupFresh(browser)` | `false` | Тесты авторизации (явно без профиля) |
| `setupWithProfile(browser)` | `true` | TC-08 (корзина требует авторизации) |

`@AfterEach tearDown()` всегда вызывает `driver.quit()` — браузер закрывается даже при падении теста.

Параметрический метод-источник:
```java
protected static Stream<Arguments> browsers() {
    return Stream.of(Arguments.of("chrome"), Arguments.of("firefox"));
}
protected static Stream<Arguments> firefoxOnly() {
    return Stream.of(Arguments.of("firefox"));
}
```

### SearchTest (9 тест-кейсов)

Аннотация `@Execution(ExecutionMode.CONCURRENT)` — тесты внутри класса запускаются параллельно.

| Тест | Запрос | Что проверяет |
|------|--------|--------------|
| TC-01 | — | Поле поиска присутствует на главной |
| TC-02 | ноутбук | Есть хотя бы одна карточка товара |
| TC-03 | смартфон | URL содержит строку запроса |
| TC-04 | телевизор | Хотя бы один заголовок содержит слово запроса |
| TC-05 | наушники | Список цен не пуст |
| TC-06 | телевизор | Страница товара: есть h1 и цена |
| TC-07 | холодильник | Страница товара: есть хлебные крошки и характеристики |
| TC-08 | мышь | Кнопка "В корзину" → тост-ответ → кнопка оформления |
| TC-09 | пылесос | Сортировка по цене → есть товары с ценами |

**TC-08 и ботозащита.** Тест использует `assumeTrue` вместо `assertTrue` для части проверок:

```java
// assumeTrue — если false, тест помечается как Skipped, а не Failed
assumeTrue(!cart.isOnAuthPage(), "Корзина требует авторизации — ботозащита");
assumeTrue(cart.hasCheckoutButton(), "Корзина пуста — товар не сохранился (ботозащита)");
```

Перед этим тест выводит значение `navigator.webdriver` в консоль, документируя причину возможного пропуска:
```java
Object webdriver = ((JavascriptExecutor) driver).executeScript("return navigator.webdriver");
System.out.println("[TC-08] navigator.webdriver = " + webdriver);
```

### AuthTest (3 негативных теста)

Тестируют форму регистрации при некорректных данных. Все используют `setupFresh` (без профиля) — если пользователь уже авторизован, Яндекс перенаправляет со страницы регистрации.

Флоу: открыть главную → нажать "Войти" → нажать "Ещё" → нажать "Создать ID" → заполнить форму → проверить ошибку.

| Тест | Входные данные | Ожидание |
|------|---------------|---------|
| `registrationWithExistingPhoneShowsError` | Существующий номер `9509579573` | Ошибка ИЛИ SMS-подтверждение |
| `registrationWithInvalidPhoneShowsError` | Некорректный номер `123` | Ошибка валидации |
| `registrationWithEmptyFieldsShowsError` | Пустая форма | Ошибка валидации |

### LoginTest (1 тест, ручной ввод)

Тест успешной авторизации. Запускается отдельно, требует участия пользователя:

```
mvn test -Dtest=LoginTest
```

**Алгоритм:**
1. Открывает Яндекс Маркет → кликает "Войти"
2. Вводит телефон `9115624293` (префикс +7 уже в форме)
3. Ждёт до 90 секунд — **два варианта:**
   - Если пуш-код пришёл быстро — пользователь вводит его сразу, тест фиксирует успех
   - Если нет — тест автоматически кликает "Отправить ещё раз" и выбирает "via SMS"
4. При SMS-пути: выводит в консоль `">>> SMS-код отправлен. Введите его в браузере..."`, ждёт до 5 минут
5. `assertTrue` — авторизация засчитывается при уходе URL с `passport.yandex`

---

## Параллельный запуск

Файл `src/test/resources/junit-platform.properties`:

```properties
junit.jupiter.execution.parallel.enabled=true
junit.jupiter.execution.parallel.mode.default=concurrent
junit.jupiter.execution.parallel.mode.classes.default=concurrent
```

Это означает: все тест-классы и все методы внутри классов (с `@Execution(CONCURRENT)`) запускаются одновременно в пуле потоков. JUnit сам подбирает размер пула по числу ядер CPU.

**Что это означает на практике:** при `mvn test` одновременно запускаются несколько экземпляров Chrome и Firefox. Каждый тест получает свой `WebDriver` (через `driver = DriverFactory.create(browser)`), они не конкурируют за один браузер.

**Ограничение:** LoginTest НЕ помечен `@Execution(CONCURRENT)` и запускается только в Firefox — он ждёт ручного ввода, параллельность не нужна.

---

## Запуск

### Все тесты

```bash
mvn test
```

### Конкретный класс

```bash
mvn test -Dtest=SearchTest
mvn test -Dtest=AuthTest
mvn test -Dtest=LoginTest   # требует ручного ввода SMS
```

### Без GUI (headless)

```bash
mvn test -Dheadless=true
```

### Подготовка профиля (для TC-08)

1. Запустить Firefox вручную с нужным профилем или скопировать `~/.mozilla/firefox/<profile>/` в `profiles/firefox/`
2. Войти в аккаунт Яндекс в браузере
3. Закрыть браузер
4. Запустить `mvn test -Dtest=SearchTest#productCanBeAddedToCart`

---

## XPath: принципы написания локаторов

Проект использует несколько стратегий для надёжных XPath:

### 1. data-auto атрибуты (предпочтительно)
```xpath
//button[@data-auto='cartButton']
```
Яндекс специально добавляет `data-auto` для автоматизации. Они стабильны.

### 2. Объединение через `|`
```xpath
//button[@data-auto='cartButton'] | //button[contains(normalize-space(.),'В корзину')]
```
Если первый вариант не найден — ищется второй.

### 3. `normalize-space()` для текста
```xpath
//button[contains(normalize-space(.),'Войти')]
```
`normalize-space()` удаляет лишние пробелы и переносы строк из текста — иначе `contains(., 'Войти')` не найдёт элемент, если в нём есть `\n`.

### 4. `translate()` для регистронезависимого поиска
```xpath
[contains(translate(normalize-space(.), 'АБВГД...', 'абвгд...'),'подороже')]
```
Конвертирует текст в нижний регистр перед сравнением.

### 5. Юникод вместо кириллицы в строковых константах
В некоторых локаторах кириллица записана escape-последовательностями:
```java
" | //input[contains(@placeholder,'Искать')]"
// эквивалентно: contains(@placeholder,'Искать')
```
Это решает проблему с кодировкой исходников в некоторых средах.

---

## Известные ограничения

| Проблема | Причина | Статус |
|----------|---------|--------|
| TC-08 часто `Skipped` | `navigator.webdriver=true` — Яндекс блокирует корзину | Документировано, используется `assumeTrue` |
| AuthTest: иногда появляется диалог "Для кого создать аккаунт" | Яндекс показывает промо при регистрации | TODO в коде |
| LoginTest запускается вручную | Требует реального SMS | Так задумано |
| Chrome профиль ~60MB | Копируется при каждом запуске TC-08 | Приемлемо для одного теста |
