# IME-actions + порядок фокуса в формах
Epic: ux-polish
Order: 04 of 08
Status: backlog
Depends-on: ux-polish-03
Date: 2026-07-06

## SPEC
=== SPEC ===
TASK: feature
PLATFORM: android
WHAT: Поля форм получают осмысленные IME-действия (Next между полями, Done на последнем) и корректный порядок фокуса — ввод многополевых форм перестаёт требовать ручных тапов по каждому полю.
LAYERS: presentation
CHANGED_HINT:
  - app/src/main/java/com/k/shavrin/diethelper/presentation/screen/product/AddProductScreen.kt:71-101 — KeyboardOptions(imeAction=Next) для промежуточных полей, Done для последнего; KeyboardActions(onNext=focusManager.moveFocus(Down), onDone=save) (G13)
  - app/src/main/java/com/k/shavrin/diethelper/presentation/screen/settings/SettingsScreen.kt:106-288 — то же для полей целей (G13)
  - app/src/main/java/com/k/shavrin/diethelper/presentation/screen/weight/WeightScreen.kt:67 — imeAction=Done + onDone=save в диалоге ввода (G13)
  - app/src/main/java/com/k/shavrin/diethelper/presentation/screen/product/ProductSearchScreen.kt:367 — imeAction=Search в строке поиска (G13)
TEST_TYPES: compose-ui
CONSTRAINTS:
  - Depends-on ux-polish-03: AddProduct/Settings/Weight-формы стабилизированы фиксом ввода (03) и undo (02) — строго после
  - FocusRequester/focusManager вводятся впервые (G13) — LocalFocusManager в composable, не в VM
  - Клавиатурный тип (Decimal/Number) НЕ менять — только добавить imeAction/KeyboardActions
  - ⚠ Same-file: ProductSearchScreen.kt/WeightScreen.kt правит и SPEC 02 (удаление confirm-диалогов) — приходит раньше по цепочке 04→03→02; SPEC 07 (haptics) идёт ПОСЛЕ 04 на этих файлах
  - onDone/onSearch переиспользуют существующие колбэки сохранения/поиска — не дублировать логику
=== END SPEC ===

## Acceptance
```gherkin
Feature: Forms have sensible keyboard actions
  Covers epic ux-polish, SPEC 04.

  @ux-polish-04
  Scenario: Next advances focus in a multi-field form
    Given the add-product form is focused on the name field
    When the user presses the keyboard Next action
    Then focus moves to the next field

  @ux-polish-04
  Scenario: Done submits the form
    Given the last field of a form is focused with valid input
    When the user presses the keyboard Done action
    Then the form is submitted

  @ux-polish-04
  Scenario: Search action triggers a product search
    Given the product search field has a query
    When the user presses the keyboard Search action
    Then the search runs
```

## Gap / context
IME-actions не заданы нигде (G13): между полями формы приходится тапать вручную, «ввод → готово» с клавиатуры не работает.

## Implementation links
- commit: (pending)
- files:  (pending)
