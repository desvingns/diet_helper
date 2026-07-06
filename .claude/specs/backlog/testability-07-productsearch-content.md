# ProductSearchContent: единый Content с табами + тесты + базлайны
Epic: testability
Order: 07 of 08
Status: backlog
Depends-on: —
Date: 2026-07-06

## SPEC
=== SPEC ===
TASK: refactor
PLATFORM: android
WHAT: Экран поиска продукта получает единый тестируемый ProductSearchContent (табы Продукты/Сохранённые, строка поиска, список) — два существующих приватных композабла становятся его внутренностями.
LAYERS: presentation
CHANGED_HINT:
  - app/src/main/java/com/k/shavrin/diethelper/presentation/screen/product/ProductSearchScreen.kt:60-146 — обёртка собирает ДВА flow (state + query :69-70, G38) и передаёт их в новый internal ProductSearchContent(state, query, onQueryChange, selectedTab, onTabChange, onProductClick, onToggleFavorite, onAddEntry, onAddNewProduct, onMealClick, onDeleteMeal); внутри — TabRow + существующие приватные ProductSearchContent :149 → переименовать (например, ProductsTab) и SavedMealsContent :207 (G29, G30; D3, @Suppress("LongParameterList") G23)
  - app/src/test/java/com/k/shavrin/diethelper/presentation/screen/product/ProductSearchScreenContentTest.kt — НОВЫЙ по G34: список продуктов из state.products виден; переключение таба показывает сохранённые блюда; пустой результат поиска показывает CTA добавления (state.hasExactMatch=false, G31); onAddEntry прокидывается (якорь 'Поиск продукта' G39)
  - app/src/test/java/com/k/shavrin/diethelper/presentation/screenshot/ProductSearchScreenScreenshotTest.kt — НОВЫЙ по G36/G49: 2 базлайна (таб продуктов с результатами + таб сохранённых), darkTheme=true (D4)
TEST_TYPES: compose-ui
CONSTRAINTS:
  - Публичная сигнатура ProductSearchScreen(date, mealType, ...) и роут product_search/{date}/{mealType} не меняются
  - Имя нового top-level Content конфликтует с существующим приватным :149 — старый ПЕРЕИМЕНОВАТЬ (не удалять функциональность); история переименования — в комментарии коммита
  - Диалог граммовки (validation grams > 0) остаётся как есть — поведение не менять
  - ⚠ data-safety-07 трогает ProductViewModel.kt (другой файл) — clash-а нет, но домен смежный: реализовывать после эпика data-safety
=== END SPEC ===

## Acceptance
```gherkin
Feature: Product search content is testable in isolation
  Covers epic testability, SPEC 07.

  @testability-07
  Scenario: Search results are listed
    Given products matching the query exist
    When the search content renders
    Then the matching products are visible

  @testability-07
  Scenario: Saved meals tab shows saved meals
    Given saved meals exist
    When the user switches to the saved-meals tab
    Then the saved meals are listed

  @testability-07 @empty
  Scenario: No exact match offers creating a product
    Given the query matches no existing product exactly
    When the search content renders
    Then an option to add a new product with that name is shown
```

## Gap / context
Два готовых приватных Content-а (G30) не тестируемы снаружи; табовая логика и CTA «добавить продукт» не покрыты UI-тестами и скриншотами.

## Implementation links
- commit: (pending)
- files:  (pending)
