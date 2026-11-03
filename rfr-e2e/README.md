# rfr-e2e

E2E-тесты Rangiffler: JUnit 6.1.3, Playwright Java 1.63 (`@UsePlaywright`), Allure 3, `JdbcClient` для фикстур.

## Запуск

Нужны JDK 25 и готовый [стенд](../README.md#с-чего-начать): MySQL `:3306`, `rfr-auth :9001`, `rfr-api :8081`, фронт `:3001`.
Команды выполняются из корня проекта.

```bash
./gradlew e2e                                          # все тесты модуля, при каждом вызове
./gradlew :rfr-e2e:test --tests '*RegistrationTest'
./gradlew :rfr-e2e:test --tests '*ProfileTest'
./gradlew :rfr-e2e:allureReport                           # rfr-e2e/build/reports/allure-report/allureReport/index.html
```

В PowerShell используй `.\gradlew.bat` вместо `./gradlew`:

```powershell
.\gradlew.bat e2e
```

### Браузеры при первом запуске

Задача `./gradlew :rfr-e2e:installPlaywright` вызывает CLI Playwright Java 1.63 с аргументами
`install --with-deps chromium` для установки Chromium и системных зависимостей в CI на Ubuntu.
При обычном UI-прогоне также доступна автоматическая установка:
`@UsePlaywright` создаёт `Playwright`, а его драйвер вызывает встроенную команду `install`, которая устанавливает
отсутствующие браузеры своей версии в кеш. Это установка браузеров зависимости, а не Gradle-задача проекта.

При первом UI-прогоне нужен доступ к сети для загрузки зависимостей и браузеров:

```powershell
.\gradlew.bat :rfr-e2e:test --tests '*RegistrationTest'
```

Не задавай `PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=1`, если используешь Chromium из комплекта Playwright
и нужная версия ещё не установлена. В обычном окружении без подмены драйвера загрузка выполняется автоматически;
отдельная установка через npm не требуется. Цепочка подтверждена исходниками зависимости 1.63:
`PlaywrightExtension` → `Playwright.create` → `PlaywrightImpl` → `DriverJar.installBrowsers`.

Проект также поддерживает уже установленный Chrome. Чтобы использовать его без загрузки браузеров Playwright,
в отдельном терминале PowerShell выполни:

```powershell
$env:PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD = '1'
$env:RFR_BROWSER_CHANNEL = 'chrome'
.\gradlew.bat :rfr-e2e:test --tests '*RegistrationTest'
```

Переменные действуют до закрытия этого терминала. Этот вариант требует установленного Chrome и не устанавливает его.

## Конфигурация

`TestConfig` читает `-Drfr.<ключ>` → `RFR_<КЛЮЧ>` → значение по умолчанию; точки в имени переменной
заменяются подчёркиваниями. Например, `rfr.base.url` соответствует `RFR_BASE_URL`. Пустые значения пропускаются.
Gradle передаёт все `-Drfr.*` в тестовую JVM.

| Ключ | По умолчанию |
|---|---|
| `rfr.base.url` | `http://localhost:3001` |
| `rfr.auth.url` | `http://localhost:9001` |
| `rfr.auth.db.url` | `jdbc:mysql://localhost:3306/rangiffler-auth?serverTimezone=UTC` |
| `rfr.api.db.url` | `jdbc:mysql://localhost:3306/rangiffler-api?serverTimezone=UTC` |
| `rfr.auth.db.username` / `rfr.auth.db.password` | `root` / `secret` |
| `rfr.api.db.username` / `rfr.api.db.password` | `root` / `secret` |
| `rfr.browser.channel` | Пустая строка: Chromium Playwright; `chrome` — установленный Chrome |

Браузер запускается без окна: `UiOptions` задаёт `headless=true`. Настройки для переключения режима через
системное свойство в проекте нет.

## Архитектура

Один source set `src/test`, пакет `io.student.rangiffler`:

| Пакет | Что лежит |
|---|---|
| `config` | `TestConfig` — единственная точка чтения настроек |
| `junit` | `annotation` (`@User`), `extension` (`UserExtension`) |
| `model` | `TestUser` |
| `fixture` | `UserFixture`, проверки очистки пользователя |
| `data` | Репозитории auth и API; `db` — `Databases`, `Jdbs` |
| `utils` | `RandomDataUtils` — случайные username/password |
| `page` | Page Objects, `component` — общие части страниц |
| `test` | Тесты регистрации, входа, профиля, JDBC и расширения JUnit |

SQL хранится в `src/test/resources/sql` и читается через `Jdbs.sql`. Миграции обеих БД должны быть выполнены
приложениями до прогона. `@User` на параметре метода создаёт учётную запись через `UserFixture` и очищает
связанные данные после теста. Учётная запись auth и API-профиль имеют разные UUID; очистка API идёт по username.
Состояние фикстур хранится в JUnit Store, без собственного `ThreadLocal`. Текущий параллельный запуск JUnit выключен
в `junit-platform.properties`; параллельность задач Gradle его не включает.

## Агент

Правила разработки — в [AGENTS.md](AGENTS.md), Page Objects — по навыку
[create-playwright-po](../.agents/skills/create-playwright-po/SKILL.md).

Проход UI вручную или через MCP не заменяет JUnit-прогон через Gradle. Успешный snackbar не доказывает сохранение:
нужно независимое чтение с сервера без клиентского кеша. Профиль и дружба сохраняются, фото и лайки — моки;
подробнее в [README API](../rfr-api/README.md#что-реализовано-а-что-mock).
