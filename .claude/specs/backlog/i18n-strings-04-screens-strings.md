# Остальные экраны + DailySummaryCard: строки в ресурсы
Epic: i18n-strings
Order: 04 of 05
Status: backlog
Depends-on: i18n-strings-02
Date: 2026-07-06

## SPEC
=== SPEC ===
TASK: refactor
PLATFORM: android
WHAT: Вторичные экраны (ProductSearch, Stats, History, HistoryDay, Weight, экранная часть AddProduct, Export) и DailySummaryCard переводят захардкоженный текст в strings.xml; ассертящие кириллицу тесты обновляются.
LAYERS: presentation
CHANGED_HINT:
  - app/src/main/res/values/strings.xml — ключи вторичных экранов (пустые состояния 'История пуста'/'Записей нет', заголовки, кнопки, лейблы DailySummaryCard 'Итог за день/Калории/Белки/Жиры/Углеводы') (G2, G12, G19)
  - app/src/main/java/com/k/shavrin/diethelper/presentation/screen/** — литералы в ProductSearchScreen, StatsScreen, HistoryScreen, HistoryDayScreen, WeightScreen, AddProductScreen, ExportScreen → stringResource (G12)
  - app/src/main/java/com/k/shavrin/diethelper/presentation/components/DailySummaryCard.kt:39-70 — label-параметры из ресурсов на стороне вызова (G19)
  - app/src/test/java/com/k/shavrin/diethelper/presentation/screen/export/ExportContentTest.kt — ассерты 'Формируем…'/'Произошла ошибка' → через ресурс (G17)
TEST_TYPES: compose-ui
CONSTRAINTS:
  - Depends-on i18n-strings-02 (общие ключи единиц/меток)
  - ⚠ Эти же файлы экранов извлекались в testability (05-08 Content) — i18n ПОСЛЕ testability; правки поверх извлечённых Content
  - ⚠ Same-file: AddProductScreen.kt / SettingsScreen.kt правит и SPEC 01 (supportingText → UiText). Валидационные сообщения — в SPEC 01, здесь ТОЛЬКО экранный текст (заголовки/кнопки/пустые состояния); выполнять ПОСЛЕ 01, зоны не пересекать
  - Roborazzi авто-подхватит ресурсы (G18); Content-тесты обновить там, где ассертят кириллицу онодами (G17)
=== END SPEC ===

## Acceptance
```gherkin
Feature: Secondary screens read text from resources
  Covers epic i18n-strings, SPEC 04.

  @i18n-strings-04 @empty
  Scenario: Empty-history message is resource-backed
    Given no history days exist
    When the history screen renders
    Then the empty message comes from a string resource

  @i18n-strings-04 @empty
  Scenario: Empty-weight message is resource-backed
    Given no weight entries exist
    When the weight screen renders
    Then the no-entries message comes from a string resource

  @i18n-strings-04
  Scenario: Daily summary labels are resource-backed
    Given the daily summary card renders
    When its macro labels are shown
    Then each label comes from a string resource
```

## Gap / context
Вторичные экраны и DailySummaryCard держат текст литералами (G12, G19); часть тестов ассертит кириллицу онодами (G17) — перевод сломает их без синхронного апдейта.

## Implementation links
- commit: (pending)
- files:  (pending)
