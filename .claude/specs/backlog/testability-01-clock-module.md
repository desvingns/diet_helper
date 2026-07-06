# ClockModule + Clock в стрик/статусы недели
Epic: testability
Order: 01 of 08
Status: backlog
Depends-on: —
Date: 2026-07-06

## SPEC
=== SPEC ===
TASK: refactor
PLATFORM: android
WHAT: Появляется инжектируемый java.time.Clock; GetStreakUseCase и GetWeekDayStatusesUseCase считают «сегодня» через него — их логика и тесты становятся детерминированными.
LAYERS: di, domain
CHANGED_HINT:
  - app/src/main/java/com/k/shavrin/diethelper/di/ClockModule.kt — НОВЫЙ: @Module @InstallIn(SingletonComponent) @Provides @Singleton fun provideClock(): Clock = Clock.systemDefaultZone(), по конвенции соседних модулей (G13; locked decision D1)
  - app/src/main/java/com/k/shavrin/diethelper/domain/usecase/foodentry/GetStreakUseCase.kt:11-16 — конструктор + clock: Clock; LocalDate.now() → LocalDate.now(clock) (G1, G9, G14)
  - app/src/main/java/com/k/shavrin/diethelper/domain/usecase/foodentry/GetWeekDayStatusesUseCase.kt:15-19,35-40 — конструктор + clock; companion computeDayStatus получает today параметром ЛИБО перестаёт быть static — выбрать при реализации, сохранив вызываемость из invoke() (G2, G10; assumption)
  - app/src/test/java/com/k/shavrin/diethelper/domain/usecase/GetStreakUseCaseTest.kt:46-49,60-155 — Clock.fixed(...) + фикс-даты вместо LocalDate.now() по прецеденту G52 (G15, G43)
  - app/src/test/java/com/k/shavrin/diethelper/domain/usecase/GetWeekDayStatusesUseCaseTest.kt:63,103,139,201 — то же (G15)
TEST_TYPES: unit
CONSTRAINTS:
  - domain остаётся чистым: java.time.Clock — JDK, НЕ android.* (архитектурное правило соблюдается)
  - Педагогический комментарий у ClockModule: зачем инжектировать время и почему Clock, а не свой интерфейс (D1)
  - Семантика стрика/статусов НЕ меняется — только источник «сегодня»; ассерты тестов переписать на фикс-даты без ослабления (никаких @Ignore)
  - CI-гейты зелёные (G36 data-safety ledger); межэпиковая связка: SPEC-и 02/03 зависят от этого модуля
=== END SPEC ===

## Acceptance
```gherkin
Feature: Deterministic streak and week statuses
  Covers epic testability, SPEC 01.

  @testability-01
  Scenario: Streak counts against an injected today
    Given the clock is fixed at 2026-03-10
    And qualifying entries exist for 2026-03-08 through 2026-03-10
    When the streak is computed
    Then the streak equals 3

  @testability-01 @edge
  Scenario: Week statuses mark days after the injected today as future
    Given the clock is fixed at 2026-03-10
    When week day statuses are computed for the week of 2026-03-09
    Then days after 2026-03-10 are reported as future
    And the computation yields the same result on every run
```

## Gap / context
GetStreakUseCase:16 и GetWeekDayStatuses:40 зовут LocalDate.now() напрямую (G1, G2): у полуночи логика «сегодня» плывёт, тесты (G15) зависят от реального времени запуска.

## Implementation links
- commit: (pending)
- files:  (pending)
