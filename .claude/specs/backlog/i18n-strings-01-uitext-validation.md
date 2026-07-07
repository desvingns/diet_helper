# sealed UiText + перенос VM-валидации в ресурсы
Epic: i18n-strings
Order: 01 of 05
Status: backlog
Depends-on: —
Date: 2026-07-06

## SPEC
=== SPEC ===
TASK: refactor
PLATFORM: android
WHAT: Вводится тип UiText, чтобы ViewModel-ы ссылались на строковые ресурсы, не держа Context; валидационные сообщения AddProduct и Settings переходят на UiText.Res и резолвятся в composable.
LAYERS: presentation
CHANGED_HINT:
  - app/src/main/java/com/k/shavrin/diethelper/presentation/util/UiText.kt — НОВЫЙ: sealed interface UiText { data class Dynamic(val value: String); data class Res(@StringRes val id: Int, val args: List<Any> = emptyList()) } + @Composable fun UiText.asString() через stringResource (G16; locked decision D2)
  - app/src/main/res/values/strings.xml — добавить ключи валидации: validation_required_name, validation_enter_number, validation_must_be_positive, validation_not_negative, validation_max_greater_min (G4)
  - app/src/main/java/com/k/shavrin/diethelper/presentation/screen/product/AddProductViewModel.kt:50-74 — RU-литералы → UiText.Res(R.string...); поля *Error в AddProductUiState: String? → UiText? (G4)
  - app/src/main/java/com/k/shavrin/diethelper/presentation/screen/settings/SettingsViewModel.kt:149-161 — то же; поля *Error в SettingsUiState → UiText? (G4)
  - app/src/main/java/com/k/shavrin/diethelper/presentation/screen/product/AddProductScreen.kt + settings/SettingsScreen.kt — supportingText резолвит UiText через asString() (G17)
  - app/src/test/java/com/k/shavrin/diethelper/presentation/screen/settings/SettingsScreenContentTest.kt — ассерты кириллицы → через ресурс/UiText (G17); AddProductViewModelTest/SettingsViewModelTest — ассерт по UiText.Res(id), не по строке
TEST_TYPES: unit, compose-ui
CONSTRAINTS:
  - Педагогический комментарий у UiText: зачем sealed-тип вместо @StringRes Int (единый способ и для динамического текста exception.message), где резолвится (только в UI-слое) — учебная ценность (D2)
  - VM НЕ получает Context (архитектурное правило G15); резолв строго в composable
  - Тесты VM ассертят UiText.Res(R.string.x), не человекочитаемую строку — устойчиво к переводу
  - detekt: без FIXME (G20)
=== END SPEC ===

## Acceptance
```gherkin
Feature: ViewModel messages reference resources via UiText
  Covers epic i18n-strings, SPEC 01.

  @i18n-strings-01 @validation
  Scenario: An empty product name yields a resource-backed error
    Given the product name field is empty
    When the user attempts to save
    Then a validation error backed by a string resource is shown for the name field

  @i18n-strings-01 @validation
  Scenario: A non-positive calorie value is rejected with a resource message
    Given the calories field is "0"
    When the user attempts to save
    Then the "must be positive" resource message is shown

  @i18n-strings-01
  Scenario: The rendered message text comes from resources
    Given a validation error is present
    When the field renders
    Then the shown text equals the string resource value
```

## Gap / context
Валидационные строки живут литералами в UiState (G4), sealed UiText не существует (G16): VM «знают» текст, локализация невозможна без Context во ViewModel.

## Implementation links
- commit: (pending)
- files:  (pending)
