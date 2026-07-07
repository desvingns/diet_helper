# Semantics для Canvas-графиков
Epic: a11y
Order: 02 of 03
Status: backlog
Depends-on: a11y-01
Date: 2026-07-06

## SPEC
=== SPEC ===
TASK: feature
PLATFORM: android
WHAT: Три Canvas-графика (пончик макросов, кольцо калорий, бары статистики) получают текстовую сводку через semantics — TalkBack озвучивает данные вместо пустоты.
LAYERS: presentation
CHANGED_HINT:
  - app/src/main/res/values/strings.xml — параметризованные ключи сводок: cd_macro_donut (белки/жиры/угл %), cd_calorie_ring (текущие/цель ккал), cd_stats_chart (калории по дням, среднее) (G3, G4, G5; D1)
  - app/src/main/java/com/k/shavrin/diethelper/presentation/screen/today/TodayScreen.kt:985 — MacroDonutChart: обернуть в Modifier.semantics { contentDescription = stringResource(...) }; цифры из state (белки/жиры/угл), НЕ из Canvas (G3, D1)
  - app/src/main/java/com/k/shavrin/diethelper/presentation/screen/today/TodayDesignedContent.kt:489 — CalorieRing: то же, сводка «X из Y ккал» (G5, D1)
  - app/src/main/java/com/k/shavrin/diethelper/presentation/screen/stats/StatsScreen.kt:356 — MacroBarLineChart: semantics со сводкой «калории по дням за период, среднее N»; формулировка для многоточечного ряда — O1 (G4, D1, O1)
TEST_TYPES: compose-ui
CONSTRAINTS:
  - Depends-on a11y-01: TodayScreen.kt/TodayDesignedContent.kt те же файлы (same-file, разные зоны) — строго после 01
  - Сводка ЛОКАЛИЗОВАНА (параметризованный ресурс с числами, H4); согласовать с ресурс-паттерном i18n (D3)
  - Цифры берутся из уже готового state графика — Canvas НЕ переписывать, только обернуть контейнер в semantics
  - Формулировка сводки баров (много дней) — на усмотрение реализации (O1), зафиксировать комментарием
  - Roborazzi авто-ок (G14)
=== END SPEC ===

## Acceptance
```gherkin
Feature: Charts expose their data to screen readers
  Covers epic a11y, SPEC 02.

  @a11y-02
  Scenario: The macro donut announces its breakdown
    Given the today screen shows the macro donut
    When a screen reader focuses the chart
    Then it announces the protein, fat and carbohydrate proportions

  @a11y-02
  Scenario: The calorie ring announces progress
    Given the calorie ring shows current and goal calories
    When a screen reader focuses the ring
    Then it announces the current calories relative to the goal

  @a11y-02 @empty
  Scenario: An empty stats chart announces no data
    Given the statistics range has no records
    When a screen reader focuses the chart area
    Then it announces that there is no data for the period
```

## Gap / context
3 Canvas-графика без semantics (G3-G5): TalkBack видит пустой прямоугольник, ключевые данные (макросы, калории, тренд) недоступны незрячим.

## Implementation links
- commit: (pending)
- files:  (pending)
