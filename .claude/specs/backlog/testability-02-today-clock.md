# Clock в TodayViewModel; «сегодня» уходит в UiState
Epic: testability
Order: 02 of 08
Status: backlog
Depends-on: testability-01
Date: 2026-07-06

## SPEC
=== SPEC ===
TASK: refactor
PLATFORM: android
WHAT: TodayViewModel получает Clock; граница «нельзя в будущее» и признак «сегодня» приходят в композаблы из состояния — TodayScreen/TodayDesignedContent больше не зовут LocalDate.now() на каждую рекомпозицию.
LAYERS: presentation
CHANGED_HINT:
  - app/src/main/java/com/k/shavrin/diethelper/presentation/screen/today/TodayViewModel.kt:48,96,102,111,122 — конструктор + clock: Clock; все 5 вызовов now() → now(clock) (G3, G11; D1)
  - app/src/main/java/com/k/shavrin/diethelper/presentation/screen/today/TodayUiState.kt:14-26 — добавить в Success поле today: LocalDate ЛИБО производный canGoForward: Boolean — выбор за разработчиком (G22; D6, O1)
  - app/src/main/java/com/k/shavrin/diethelper/presentation/screen/today/TodayScreen.kt:443 — убрать val today = LocalDate.now(); использовать значение из state (G4, G20; D6)
  - app/src/main/java/com/k/shavrin/diethelper/presentation/screen/today/TodayDesignedContent.kt:247 — то же (G4, G20; D6)
  - app/src/main/java/com/k/shavrin/diethelper/presentation/screen/history/HistoryDayScreen.kt:56 — потребитель TodayContent(readOnly=true): обновить вызов под новую сигнатуру (G26)
  - app/src/test/java/com/k/shavrin/diethelper/presentation/screen/today/TodayScreenContentTest.kt — обновить вызовы TodayContent под новую сигнатуру (G34)
  - app/src/test/java/com/k/shavrin/diethelper/presentation/screenshot/TodayScreenScreenshotTest.kt — обновить вызов TodayContent; recordRoborazziDebug только если пиксели реально изменились (G36)
  - app/src/test/java/com/k/shavrin/diethelper/presentation/screen/today/TodayViewModelTest.kt:76-212 — Clock.fixed + фикс-даты вместо 8 live-вызовов (G16; D2, прецедент G52)
TEST_TYPES: unit, compose-ui
CONSTRAINTS:
  - Смена сигнатуры TodayContent затрагивает его потребителя HistoryDayScreen:56 (readOnly-режим, G26) и существующие TodayScreenContentTest/TodayScreenScreenshotTest (G34, G36) — обновить вызовы, базлайн пересматривать только если пиксели реально изменились (recordRoborazziDebug осознанно)
  - Кламп будущих дат — поведение сохраняется 1:1; меняется только источник времени
  - ⚠ межэпиковый clash: i18n-strings и a11y эпики тоже тронут TodayScreen.kt — testability идёт раньше (порядок эпиков на доске)
=== END SPEC ===

## Acceptance
```gherkin
Feature: Today screen boundary is state-driven
  Covers epic testability, SPEC 02.

  @testability-02
  Scenario: Navigation beyond the injected today is clamped
    Given the clock is fixed at 2026-03-10
    And the user is viewing 2026-03-10
    When the user tries to go to the next day
    Then the visible date remains 2026-03-10

  @testability-02
  Scenario: Yesterday allows forward navigation
    Given the clock is fixed at 2026-03-10
    And the user is viewing 2026-03-09
    When the user goes to the next day
    Then the visible date becomes 2026-03-10

  @testability-02 @edge
  Scenario: Rendering does not consult the wall clock
    Given the clock is fixed at 2026-03-10
    When the today screen renders repeatedly
    Then the today boundary shown is identical on every render
```

## Gap / context
TodayViewModel держит 5 вызовов now() (G3), а обе композиции читают wall-clock на каждую рекомпозицию (G4, G20) — в полночь UI и VM могут разойтись во мнении, что такое «сегодня».

## Implementation links
- commit: (pending)
- files:  (pending)
