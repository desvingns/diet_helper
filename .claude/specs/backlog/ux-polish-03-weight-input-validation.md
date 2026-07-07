# Видимая ошибка невалидного ввода веса
Epic: ux-polish
Order: 03 of 08
Status: backlog
Depends-on: ux-polish-02
Date: 2026-07-06

## SPEC
=== SPEC ===
TASK: bugfix
PLATFORM: android
WHAT: Невалидный ввод веса (пусто/ноль/не число) показывает ошибку вместо молчаливого игнорирования — WeightViewModel принимает паттерн отслеживания ошибок из SettingsViewModel.
LAYERS: presentation
CHANGED_HINT:
  - app/src/main/java/com/k/shavrin/diethelper/presentation/screen/weight/WeightViewModel.kt:61-67 — save(): вместо тихого return при value==null||<=0 выставлять inputError (G11; образец SettingsViewModel:83-88 G12)
  - app/src/main/java/com/k/shavrin/diethelper/presentation/screen/weight/WeightUiState.kt — добавить inputError: UiText? (или @StringRes) в состояние ввода; WeightUiState.Error уже есть (:13), но это ошибка загрузки, не ввода — нужно отдельное поле (G11)
  - app/src/main/java/com/k/shavrin/diethelper/presentation/screen/weight/WeightScreen.kt — поле ввода показывает inputError (supportingText/isError), очищается при правке (G12)
  - app/src/test/java/com/k/shavrin/diethelper/presentation/screen/weight/WeightViewModelTest.kt — кейсы: пустой/ноль/нечисло → inputError выставлен, upsert НЕ вызван
TEST_TYPES: unit, compose-ui
CONSTRAINTS:
  - Depends-on ux-polish-02: WeightViewModel/WeightScreen правит и 02 (undo) — строго после
  - Сообщение ошибки — через ресурс/UiText (i18n прошёл, паттерн UiText из i18n-01, H4)
  - comma→dot (replace(',','.')) сохранить (G12); менять только ветку невалидного значения
  - Не смешивать с WeightUiState.Error (загрузка списка) — это ошибка ПОЛЯ ввода
=== END SPEC ===

## Acceptance
```gherkin
Feature: Invalid weight input is surfaced
  Covers epic ux-polish, SPEC 03.

  @ux-polish-03 @validation
  Scenario: Empty weight shows an error
    Given the weight field is empty
    When the user tries to save
    Then an input error is shown and nothing is saved

  @ux-polish-03 @validation
  Scenario Outline: Non-positive or non-numeric input is rejected
    Given the weight field contains "<value>"
    When the user tries to save
    Then an input error is shown and nothing is saved

    Examples:
      | value |
      | 0     |
      | abc   |
      | -5    |

  @ux-polish-03
  Scenario: A valid weight saves and clears any error
    Given the weight field contains "82,5"
    When the user saves
    Then the weight is saved and no input error remains
```

## Gap / context
WeightViewModel.save() молча игнорирует невалидный ввод (G11): пользователь жмёт «сохранить», ничего не происходит и не объясняется.

## Implementation links
- commit: (pending)
- files:  (pending)
