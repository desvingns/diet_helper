# androidTest-скаффолд + Hilt smoke-тест запуска
Epic: quality-gates
Order: 04 of 05
Status: backlog
Depends-on: quality-gates-01
Date: 2026-07-06

## SPEC
=== SPEC ===
TASK: feature
PLATFORM: android
WHAT: Появляется инструментальный набор androidTest с одним Hilt-smoke: приложение запускается с реальным Hilt-графом и не падает на первом кадре — ловит DI/manifest/dex-ошибки, невидимые юнит-тестам.
LAYERS: build, test
CHANGED_HINT:
  - gradle/libs.versions.toml — добавить hilt-android-testing (= версии Hilt 2.55), androidx.test.ext:junit, androidx.test:runner, androidx.compose.ui:ui-test-junit4 (из Compose BOM) (G8, H4)
  - app/build.gradle.kts — androidTestImplementation этих зависимостей; testInstrumentationRunner → "com.k.shavrin.diethelper.HiltTestRunner" (сейчас дефолтный AndroidJUnitRunner, G16); kaptAndroidTest/kspAndroidTest hilt-compiler
  - app/src/androidTest/java/com/k/shavrin/diethelper/HiltTestRunner.kt — НОВЫЙ: AndroidJUnitRunner override newApplication → HiltTestApplication (стандартный паттерн, G17)
  - app/src/androidTest/java/com/k/shavrin/diethelper/AppLaunchSmokeTest.kt — НОВЫЙ: @HiltAndroidTest, @get:Rule HiltAndroidRule + ActivityScenarioRule<MainActivity>, тест «MainActivity стартует и корневой узел отображён» (G17, G18)
TEST_TYPES: instrumented
CONSTRAINTS:
  - Depends-on quality-gates-01: тот же build.gradle.kts (same-file clash) — 01 первым, здесь только androidTest-блок зависимостей
  - androidTest в CI НЕ запускаем — нет эмулятора в workflow (D5); SPEC добавляет только компилируемый скаффолд + локально-запускаемый тест; connectedCheck-джоб — будущая отдельная тема
  - Это ПРЯМОЙ ответ на класс «dex/DI-крэш проходит все юнит-гейты» (ср. MyMoney RecurringWorker) — smoke обязан реально стартовать граф, а не инспектировать исходники (в отличие от AppNavHostTest, G18)
  - Fakes-only относится к юнит-слою; здесь реальный Hilt-граф (инструментальный тест) — это ОК и намеренно
=== END SPEC ===

## Acceptance
```gherkin
Feature: Instrumented Hilt smoke test
  Covers epic quality-gates, SPEC 04.

  @quality-gates-04
  Scenario: The app launches with the real dependency graph
    Given the app is installed on a device or emulator
    When the main activity starts
    Then the Hilt graph resolves and the main screen is displayed
    And the app does not crash on first render

  @quality-gates-04 @error
  Scenario: A broken dependency graph fails the smoke test
    Given a dependency is missing from the graph
    When the smoke test launches the app
    Then the test fails instead of shipping a crash
```

## Gap / context
androidTest/ не существует (G16); AppNavHostTest лишь читает исходник как текст (G18) — реального запуска Hilt-графа нет, DI/dex-крэши на старте проходят все текущие гейты.

## Implementation links
- commit: (pending)
- files:  (pending)
