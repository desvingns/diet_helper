# AddProductContent: извлечение + тест валидации + базлайны
Epic: testability
Order: 06 of 08
Status: backlog
Depends-on: —
Date: 2026-07-06

## SPEC
=== SPEC ===
TASK: refactor
PLATFORM: android
WHAT: Форма нового продукта извлекается в AddProductContent(state, колбэки) — валидационные состояния формы пиннятся Compose-тестом и двумя базлайнами.
LAYERS: presentation
CHANGED_HINT:
  - app/src/main/java/com/k/shavrin/diethelper/presentation/screen/product/AddProductScreen.kt:27-112 — AddProductScreen(initialName, onNavigateBack, viewModel) остаётся обёрткой (viewModel.state :34 — имя потока именно state, G38); тело (Column с 5 OutlinedTextField + Button) → internal AddProductContent(state: AddProductUiState, onNameChange, onCaloriesChange, onProteinChange, onFatChange, onCarbsChange, onSave) (G27, G28; паттерн G21, @Suppress("LongParameterList") по G23)
  - app/src/test/java/com/k/shavrin/diethelper/presentation/screen/product/AddProductScreenContentTest.kt — НОВЫЙ по G34: поля отображают state; ошибка поля видна (supportingText); кнопка 'Сохранить' дизейблится при isSaving; onSave вызывается по клику (якорь 'Новый продукт' G39)
  - app/src/test/java/com/k/shavrin/diethelper/presentation/screenshot/AddProductScreenScreenshotTest.kt — НОВЫЙ по G36/G49: 2 базлайна (чистая форма + форма с ошибками валидации), darkTheme=true (D4)
TEST_TYPES: compose-ui
CONSTRAINTS:
  - Публичная сигнатура AddProductScreen и роут add_product?name= не меняются
  - Тексты ошибок валидации живут во ViewModel — здесь их НЕ трогать (уйдут в ресурсы эпиком i18n-strings)
  - Новые базлайны коммитятся; CI verify зелёный (G48)
=== END SPEC ===

## Acceptance
```gherkin
Feature: Add-product form is testable in isolation
  Covers epic testability, SPEC 06.

  @testability-06
  Scenario: Filled form saves the product
    Given the form is filled with a name and valid macros
    When the user confirms saving
    Then the save action is invoked once

  @testability-06 @validation
  Scenario: Field errors are visible
    Given the calories field carries a validation error
    When the form renders
    Then the calories error text is shown under the field

  @testability-06 @edge
  Scenario: Saving state blocks a second submit
    Given the form is in the saving state
    When the form renders
    Then the save action is disabled
```

## Gap / context
AddProductScreen — inline-форма без извлечения (G27): валидационная UX (ошибки полей, блокировка кнопки) не запинена ни одним UI-тестом.

## Implementation links
- commit: (pending)
- files:  (pending)
