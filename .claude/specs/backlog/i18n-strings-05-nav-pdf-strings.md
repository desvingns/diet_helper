# Навигация + PDF: строки в ресурсы, объединение меток
Epic: i18n-strings
Order: 05 of 05
Status: backlog
Depends-on: i18n-strings-02
Date: 2026-07-06

## SPEC
=== SPEC ===
TASK: refactor
PLATFORM: android
WHAT: Лейблы нижней навигации и весь текст PDF-отчёта переезжают в ресурсы; PdfReportRenderer читает их через context.getString; метки приёмов пищи в PDF и UI ссылаются на один ресурс.
LAYERS: presentation, data
CHANGED_HINT:
  - app/src/main/res/values/strings.xml — nav_today/history/stats/weight/settings; ключи PDF: report_title, report_generated, report_daily_goals, report_totals_by_day, report_entries_by_day, report_calories_by_day, report_no_entries, колонки Дата/ккал/Б/Ж/У и т.д. (G5, G6)
  - app/src/main/java/com/k/shavrin/diethelper/presentation/navigation/BottomNavItem.kt:16-20 — 5 лейблов → @StringRes id в sealed class; резолв в composable нижней панели (G6)
  - app/src/main/java/com/k/shavrin/diethelper/data/pdf/PdfReportLayout.kt:41-44 — MEAL_LABEL_* удалить, использовать общий meal_* ресурс (H4, G14)
  - app/src/main/java/com/k/shavrin/diethelper/data/pdf/PdfReportRenderer.kt:138-378 — ~20 литералов → context.getString(R.string...); Context уже в конструкторе (G13, D5)
TEST_TYPES: unit
CONSTRAINTS:
  - Depends-on i18n-strings-02: meal_* ключи заведены там; здесь ПЕРЕИСПОЛЬЗОВАТЬ, устранив дубль Format↔PDF (H4, G14)
  - BottomNavItem — sealed class: хранить @StringRes Int, НЕ String (резолв в UI); проверить, что маршруты/иконки не задеты
  - PdfReportRenderer в data-слое с Context — это ОК (renderer уже держит @ApplicationContext G13); domain-интерфейс ReportRenderer НЕ трогать
  - PdfReportLayoutTest — обновить, если проверяет строковые константы; локаль дат в PDF — отдельная тема, не трогать
=== END SPEC ===

## Acceptance
```gherkin
Feature: Navigation and PDF text read from resources
  Covers epic i18n-strings, SPEC 05.

  @i18n-strings-05
  Scenario: Bottom navigation labels are resource-backed
    Given the app shows the bottom navigation
    When the tabs render
    Then each tab label comes from a string resource

  @i18n-strings-05
  Scenario: The PDF report title is resource-backed
    Given the user exports a report
    When the PDF is generated
    Then the report title text comes from a string resource

  @i18n-strings-05
  Scenario: Meal labels are shared between the app and the PDF
    Given a meal type appears both on a screen and in the exported report
    When both are rendered
    Then they show the same meal label from a single shared resource

  @i18n-strings-05 @empty
  Scenario: An empty report range uses the resource-backed no-entries text
    Given the user exports a report for a range with no records
    When the PDF is generated
    Then the "no entries" text comes from a string resource
```

## Gap / context
Навигация (G6) и ~20 строк PDF (G5) захардкожены; метки приёмов пищи дублируются между Format.kt и PDF (G14) — два источника правды для одного и того же текста.

## Implementation links
- commit: (pending)
- files:  (pending)
