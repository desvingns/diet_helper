# Progress-semantics + не-цветовой сигнал + touch-target
Epic: a11y
Order: 03 of 03
Status: backlog
Depends-on: a11y-02
Date: 2026-07-06

## SPEC
=== SPEC ===
TASK: feature
PLATFORM: android
WHAT: Индикаторы прогресса озвучиваются (progressSemantics/stateDescription), прогресс макросов несёт не только цвет (число/процент — WCAG 1.4.1), текст-кликабельные получают минимальный touch-target 48dp.
LAYERS: presentation
CHANGED_HINT:
  - app/src/main/java/com/k/shavrin/diethelper/presentation/components/DailySummaryCard.kt:95 — LinearProgressIndicator: progress-семантика + stateDescription «N% от цели»; рядом с цветом — число/процент как не-цветовой канал (G7, G9; D2)
  - app/src/main/java/com/k/shavrin/diethelper/presentation/screen/today/TodayDesignedContent.kt:608 — MacroRangeProgress (кастомный Canvas): Modifier.semantics { progressBarRangeInfo/stateDescription } (G6, D2)
  - app/src/main/java/com/k/shavrin/diethelper/presentation/util/MacroColorUtil.kt:10,22 — задокументировать, что цвет — ВСПОМОГАТЕЛЬНЫЙ канал; основной сигнал (число/процент) обеспечивают вызывающие композаблы (G9; D2)
  - app/src/main/java/com/k/shavrin/diethelper/presentation/screen/today/TodayScreen.kt:461,469 — текст-кликабельные (дата, 'Сегодня'): Modifier.minimumInteractiveComponentSize() или sizeIn(minHeight=48.dp) (G11, H3)
  - app/src/main/res/values/strings.xml — ключи stateDescription прогресса (параметр %) (G7, G9; D2)
TEST_TYPES: compose-ui
CONSTRAINTS:
  - Depends-on a11y-02: TodayScreen.kt/TodayDesignedContent.kt/DailySummaryCard те же файлы — строго после 02
  - touch-target не должен сдвинуть визуал — проверить Roborazzi после (паддинг вокруг текста); при сдвиге пикселей recordRoborazziDebug осознанно (G14, H3)
  - MacroColorUtil остаётся internal util (не менять сигнатуры цветовых функций) — добавляется только не-цветовой канал на стороне UI (D2)
  - CircularProgressIndicator loading (G8) — добавить stateDescription «загрузка»; это индикатор занятости, не прогресс-бар
=== END SPEC ===

## Acceptance
```gherkin
Feature: Progress is perceivable without color and by screen readers
  Covers epic a11y, SPEC 03.

  @a11y-03
  Scenario: Macro progress announces its percentage
    Given the daily summary shows macro progress bars
    When a screen reader focuses a macro bar
    Then it announces the percentage of the goal reached

  @a11y-03
  Scenario: Progress is distinguishable without color
    Given a macro is over its goal
    When the progress is shown
    Then the over-goal state is conveyed by text or number, not color alone

  @a11y-03 @edge
  Scenario: Text shortcuts meet the minimum touch target
    Given the today screen shows the date and "today" text shortcuts
    When their interactive size is measured
    Then each is at least 48dp in height
```

## Gap / context
Прогресс-индикаторы без semantics (G6-G8), макро-прогресс различим только цветом (G9, WCAG 1.4.1), текст-кликабельные меньше 48dp (G11) — недоступно незрячим и людям с нарушением цветовосприятия/моторики.

## Implementation links
- commit: (pending)
- files:  (pending)
