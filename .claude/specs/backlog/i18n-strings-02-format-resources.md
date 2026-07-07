# Format.kt: дни/месяцы/единицы/plural в ресурсы
Epic: i18n-strings
Order: 02 of 05
Status: backlog
Depends-on: —
Date: 2026-07-06

## SPEC
=== SPEC ===
TASK: refactor
PLATFORM: android
WHAT: Названия дней недели, месяцев (родительный падеж), метки приёмов пищи, единицы измерения и plural-склонения переезжают в строковые ресурсы; Format.kt остаётся чистым, резолв — в composable.
LAYERS: presentation
CHANGED_HINT:
  - app/src/main/res/values/strings.xml — string-array days_of_week (7), string-array months_genitive (12), meal_breakfast/lunch/dinner/snack (общие ключи — их же использует SPEC 05 для PDF, H4), unit_grams/unit_kcal/unit_kg, plurals streak_days (quantity strings) (G3, G7, G14)
  - app/src/main/java/com/k/shavrin/diethelper/presentation/util/Format.kt:13-66 — dayOfWeekNames/monthGenitiveNames Map и mealTypeLabel() перестают хранить литералы: возвращают индекс/@StringRes, ЛИБО переезжают в @Composable-хелперы со stringResource/stringArrayResource; единицы в formatGrams/Calories/Macro/Weight — через ресурс (G3; D4, O1)
  - вызовы Format.* в composables — резолвят через stringResource/pluralStringResource (daysWord → pluralStringResource, H5)
TEST_TYPES: unit, compose-ui
CONSTRAINTS:
  - Format остаётся чистым (без Context) — если функция обязана вернуть готовую строку, она переезжает в UI-хелпер; выбор способа (@StringRes vs stringArrayResource+index) за разработчиком (O1), зафиксировать комментарием
  - Ключи meal_* — ОБЩИЕ с SPEC 05 (PDF): согласовать имена, не плодить дубликаты (H4, G14)
  - FormatTest (существующий) обновить: проверять индекс/ресурс-id, не человекочитаемую строку; числовой формат (разделитель) НЕ трогать
  - ⚠ strings.xml трогают также 03/04/05 — аддитивно; 02 кладёт базовые ключи первым (Depends-on-цель для 03-05)
=== END SPEC ===

## Acceptance
```gherkin
Feature: Formatting helpers read from resources
  Covers epic i18n-strings, SPEC 02.

  @i18n-strings-02
  Scenario: A weekday label comes from resources
    Given a date falling on Monday
    When its weekday label is rendered
    Then the label equals the Monday string resource

  @i18n-strings-02
  Scenario: A calorie value shows the resource unit
    Given a calorie amount of 500
    When it is formatted
    Then it renders with the kcal unit from resources

  @i18n-strings-02 @edge
  Scenario Outline: Streak day count uses plural resources
    Given a streak of <n> days
    When the streak label renders
    Then it uses the plural resource for <n>

    Examples:
      | n |
      | 1 |
      | 3 |
      | 5 |
```

## Gap / context
Format.kt хранит дни/месяцы/единицы литералами (G3), plural склеивается вручную (G7) — не локализуемо, не использует quantity-strings.

## Implementation links
- commit: (pending)
- files:  (pending)
