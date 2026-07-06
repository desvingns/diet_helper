# HistoryContent: извлечение + тесты + базлайны
Epic: testability
Order: 05 of 08
Status: backlog
Depends-on: —
Date: 2026-07-06

## SPEC
=== SPEC ===
TASK: refactor
PLATFORM: android
WHAT: Из HistoryScreen извлекается тестируемый HistoryContent(state, onItemClick); появляются первый Compose-тест и 2 Roborazzi-базлайна экрана истории.
LAYERS: presentation
CHANGED_HINT:
  - app/src/main/java/com/k/shavrin/diethelper/presentation/screen/history/HistoryScreen.kt:34-100 — HistoryScreen(onNavigateToDay, viewModel) остаётся тонкой обёрткой (hiltViewModel + collectAsState :39); тело → internal HistoryContent(state: HistoryUiState, onItemClick: (LocalDate) -> Unit), внутри существующие приватные HistoryList :57 и EmptyHistoryState :87 (G24, G25; паттерн G21)
  - app/src/test/java/com/k/shavrin/diethelper/presentation/screen/history/HistoryScreenContentTest.kt — НОВЫЙ по паттерну G34: Success-список кликабелен (onItemClick получает дату), пустой Success показывает 'История пуста' (якорь G39), Error показывает сообщение
  - app/src/test/java/com/k/shavrin/diethelper/presentation/screenshot/HistoryScreenScreenshotTest.kt — НОВЫЙ по паттерну G36/G49: 2 базлайна (populated + empty), darkTheme=true, resizeScale=0.5 (D4)
TEST_TYPES: compose-ui
CONSTRAINTS:
  - Сигнатура публичного HistoryScreen НЕ меняется (навигация в AppNavHost нетронута)
  - detekt LongMethod=120 (G50): при необходимости оставить/добавить приватные под-композаблы
  - Новые PNG-базлайны коммитятся (recordRoborazziDebug), CI verify обязан пройти (G48)
  - ⚠ межэпиковый порядок: i18n-strings позже тронет этот же файл — строки здесь НЕ выносить
=== END SPEC ===

## Acceptance
```gherkin
Feature: History content is testable in isolation
  Covers epic testability, SPEC 05.

  @testability-05
  Scenario: A history day opens its detail
    Given the history shows recorded days
    When the user selects a day
    Then the day detail is requested for that date

  @testability-05 @empty
  Scenario: Empty history explains itself
    Given no days are recorded
    When the history content renders
    Then the empty-history message is shown

  @testability-05 @error
  Scenario: History error is visible
    Given history loading failed
    When the history content renders
    Then the error message is shown
```

## Gap / context
HistoryScreen — UI без Content-извлечения (G24): рендер-поведение (пустое состояние, клики) не покрыто ни Compose-тестом, ни скриншотом.

## Implementation links
- commit: (pending)
- files:  (pending)
