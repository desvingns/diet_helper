# Добить SettingsViewModelTest (переходы состояния и валидация)
Epic: testability
Order: 04 of 08
Status: backlog
Depends-on: —
Date: 2026-07-06

## SPEC
=== SPEC ===
TASK: feature
PLATFORM: android
WHAT: SettingsViewModelTest покрывает контракт сохранения целиком: переходы isSaving→justSaved через Turbine (вместо гоночного синхронного чтения), краевую валидацию и сброс justSaved при правке поля. Прод-код НЕ меняется.
LAYERS: test-only
CHANGED_HINT:
  - app/src/test/java/com/k/shavrin/diethelper/presentation/screen/settings/SettingsViewModelTest.kt:44-188 — добавить кейсы: (1) save() эмитит isSaving=true → isSaving=false + justSaved=true, ассерты через state-Flow + Turbine, а не state.value (G45, G46, G47); (2) validateRange(min=null, max=null) и прочие null-краи (G45); (3) изменение любого поля после успешного save сбрасывает justSaved (G45); (4) невалидный ввод НЕ вызывает saveGoals у FakeGoalsRepository
  - там же — переиспользовать MainDispatcherRule + Turbine по домашнему паттерну (G40)
TEST_TYPES: unit
CONSTRAINTS:
  - Прод-поведение «сбой saveGoals не ловится» (G46) НЕ тестировать и НЕ чинить — error-surfacing принадлежит O1 data-safety/ux-polish (H2 grill); писать тест на несуществующий контракт нельзя
  - Fakes-only: только существующий FakeGoalsRepository; никаких мок-фреймворков
  - Существующие 15 тест-методов не ослаблять и не удалять — только дополнять
=== END SPEC ===

## Acceptance
```gherkin
Feature: Settings save contract is fully pinned
  Covers epic testability, SPEC 04.

  @testability-04
  Scenario: Saving valid goals reports progress and success
    Given valid goal values are entered
    When the user saves the goals
    Then a saving-in-progress state is observed
    And it is followed by a saved-successfully state

  @testability-04 @validation
  Scenario: Invalid goals are not persisted
    Given the calories field is empty
    When the user saves the goals
    Then a field error is shown
    And the stored goals remain unchanged

  @testability-04 @edge
  Scenario: Editing a field clears the saved confirmation
    Given the goals were just saved successfully
    When the user edits the protein field
    Then the saved confirmation is no longer shown
```

## Gap / context
SettingsViewModelTest минимален и читает state синхронно (G47) — переходы isSaving/justSaved и null-краи валидации (G45) не запинены; регресс в save-флоу тесты не поймают.

## Implementation links
- commit: (pending)
- files:  (pending)
