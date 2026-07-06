# Clock в StatsViewModel, ExportViewModel, WeightViewModel
Epic: testability
Order: 03 of 08
Status: backlog
Depends-on: testability-01
Date: 2026-07-06

## SPEC
=== SPEC ===
TASK: refactor
PLATFORM: android
WHAT: Три оставшихся ViewModel считают «сегодня» через инжектированный Clock; их тесты переходят на фикс-даты и перестают зависеть от момента запуска.
LAYERS: presentation
CHANGED_HINT:
  - app/src/main/java/com/k/shavrin/diethelper/presentation/screen/stats/StatsViewModel.kt:23-24,67-73 — конструктор + clock; 6 вызовов now() → now(clock) (G5; D1)
  - app/src/main/java/com/k/shavrin/diethelper/presentation/screen/export/ExportViewModel.kt:109 — конструктор + clock; initialState() через now(clock) (G7; D1)
  - app/src/main/java/com/k/shavrin/diethelper/presentation/screen/weight/WeightViewModel.kt:48,65 — конструктор + clock; префилл и save() через now(clock) (G6, G12; D1)
  - app/src/test/java/com/k/shavrin/diethelper/presentation/screen/stats/StatsViewModelTest.kt — Clock.fixed + фикс-даты вместо live-ассертов (G44; D2, G52)
  - app/src/test/java/com/k/shavrin/diethelper/presentation/screen/export/ExportViewModelTest.kt:56-199 — то же (G16)
  - app/src/test/java/com/k/shavrin/diethelper/presentation/screen/weight/WeightViewModelTest.kt:32-105 — то же (G17)
TEST_TYPES: unit
CONSTRAINTS:
  - Пользовательское поведение не меняется: дефолтные диапазоны (7 дней Stats/Export) и дата записи веса остаются «сегодня» — детерминированным становится только источник
  - ExportViewModel: не трогать Uri.Builder-блок (Windows-Robolectric quirk, факт G25 эпика data-safety) — скоуп строго время
  - Тесты: skipLoading()-хелперы остаются локальными per-file (H4 grill) — не выносить в общий util в этом SPEC-е
  - ⚠ testability-08 (WeightContent) зависит от этого SPEC-а: конструктор WeightViewModel стабилизируется здесь
=== END SPEC ===

## Acceptance
```gherkin
Feature: Deterministic default ranges and weight dates
  Covers epic testability, SPEC 03.

  @testability-03
  Scenario: Stats default range ends at the injected today
    Given the clock is fixed at 2026-03-10
    When the statistics screen initializes
    Then the range covers 2026-03-04 through 2026-03-10

  @testability-03
  Scenario: Export default range ends at the injected today
    Given the clock is fixed at 2026-03-10
    When the export screen initializes
    Then the proposed range ends on 2026-03-10

  @testability-03
  Scenario: Saving weight records it on the injected today
    Given the clock is fixed at 2026-03-10
    When the user saves weight "82,5"
    Then a weight entry exists for 2026-03-10 with value 82.5

  @testability-03 @edge
  Scenario: Default range spans a month boundary correctly
    Given the clock is fixed at 2026-03-02
    When the statistics screen initializes
    Then the range covers 2026-02-24 through 2026-03-02
```

## Gap / context
StatsViewModel (6 точек, G5), ExportViewModel (G7) и WeightViewModel (G6) читают wall-clock напрямую; их тесты (G44, G16, G17) сравнивают с live-now() и падают на границе суток.

## Implementation links
- commit: (pending)
- files:  (pending)
