# Today-кластер: строки в ресурсы
Epic: i18n-strings
Order: 03 of 05
Status: backlog
Depends-on: i18n-strings-02
Date: 2026-07-06

## SPEC
=== SPEC ===
TASK: refactor
PLATFORM: android
WHAT: Самый крупный кластер захардкоженного текста (TodayScreen + TodayDesignedContent, ~97 строк) переезжает в strings.xml и читается через stringResource.
LAYERS: presentation
CHANGED_HINT:
  - app/src/main/res/values/strings.xml — ключи Today: 'ПРИЁМЫ ПИЩИ', 'Сегодня', 'Выбрать'/'Отмена', макро-лейблы, 'ИЗ … ККАЛ' (параметризованный), 'Начните серию', меню 'Копировать/Сохранить/Удалить', 'Удалить запись?' и пр. (G2, G7)
  - app/src/main/java/com/k/shavrin/diethelper/presentation/screen/today/TodayScreen.kt — 52 литерала → stringResource (G2)
  - app/src/main/java/com/k/shavrin/diethelper/presentation/screen/today/TodayDesignedContent.kt — 45 литералов → stringResource; daysWord → pluralStringResource из SPEC 02 (G7, H5)
  - переиспользовать meal_* и unit_* ключи из SPEC 02 (не дублировать, H4)
TEST_TYPES: compose-ui
CONSTRAINTS:
  - Depends-on i18n-strings-02: общие ключи меток/единиц/plural должны существовать
  - ⚠ Эти файлы правил эпик testability (Clock в TodayViewModel:02, извлечение не для Today) — i18n идёт ПОСЛЕ testability (порядок эпиков), rebase поверх извлечённых Content
  - TodayScreenContentTest/TodayScreenScreenshotTest: строки читаются из ресурсов в рантайме → Roborazzi авто-ок (G18); Content-тест обновить, если ассертит кириллицу онодами
  - detekt MaxLineLength=200 — stringResource в лимите (G20)
=== END SPEC ===

## Acceptance
```gherkin
Feature: Today screens read text from resources
  Covers epic i18n-strings, SPEC 03.

  @i18n-strings-03
  Scenario: The meal-sections header comes from resources
    Given the today screen renders
    When the meal sections are shown
    Then the sections header text comes from a string resource

  @i18n-strings-03 @empty
  Scenario: The empty-streak prompt is resource-backed
    Given the streak is zero
    When the today screen renders
    Then the "start a streak" prompt comes from a string resource

  @i18n-strings-03
  Scenario: The delete-entry dialog title is resource-backed
    Given the user long-presses an entry
    When the delete dialog opens
    Then its title comes from a string resource
```

## Gap / context
Today — треть всего хардкода (97/301, G2): без выноса локализация экрана невозможна, дубли меток с PDF/Format сохраняются.

## Implementation links
- commit: (pending)
- files:  (pending)
