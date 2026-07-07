# @Preview для экранов без превью
Epic: ux-polish
Order: 08 of 08
Status: backlog
Depends-on: —
Date: 2026-07-06

## SPEC
=== SPEC ===
TASK: feature
PLATFORM: android
WHAT: Пять экранов без превью (ProductSearch, AddProduct, Settings, Stats, Export) получают @Preview (light/dark) — единообразное покрытие для быстрой визуальной итерации в Android Studio.
LAYERS: presentation
CHANGED_HINT:
  - app/src/main/java/com/k/shavrin/diethelper/presentation/screen/Previews.kt:26-142 — добавить @Preview-функции для ProductSearchContent, AddProductContent, SettingsScreenContent, StatsScreenContent, ExportContent (light/dark, как у существующих 5, G16)
  - использовать извлечённые *Content(state, ...) из эпика testability (05-08) — превью рендерят Content с фейковым state (G16)
TEST_TYPES: compose-ui
CONSTRAINTS:
  - Depends-on (межэпиковый): testability извлёк *Content для History/AddProduct/ProductSearch/Weight; Settings/Stats/Export Content уже есть — превью используют их (не создают новые Content)
  - Превью НЕ участвуют в Roborazzi-верификации (это @Preview для Studio, не тесты); отдельные скриншот-базлайны — в testability-эпике
  - Фейковый state для превью — реалистичный (заполненное состояние), по образцу существующих превью Today/Weight/History (G16)
  - Независимый SPEC — трогает только Previews.kt
=== END SPEC ===

## Acceptance
```gherkin
Feature: Preview coverage for all screens
  Covers epic ux-polish, SPEC 08.

  @ux-polish-08
  Scenario: Every primary screen has a preview
    Given the previews file
    When a developer opens it in the IDE
    Then ProductSearch, AddProduct, Settings, Stats and Export each render a preview

  @ux-polish-08
  Scenario: Previews render in both light and dark
    Given a screen preview
    When it is displayed
    Then both light and dark variants are available
```

## Gap / context
5 из 10 экранов без @Preview (G16): ProductSearch/AddProduct/Settings/Stats/Export нельзя быстро посмотреть в Studio без запуска приложения.

## Implementation links
- commit: (pending)
- files:  (pending)
