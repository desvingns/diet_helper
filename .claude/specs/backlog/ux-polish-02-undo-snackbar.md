# Undo-snackbar на удаления (re-insert)
Epic: ux-polish
Order: 02 of 08
Status: backlog
Depends-on: ux-polish-01
Date: 2026-07-06

## SPEC
=== SPEC ===
TASK: feature
PLATFORM: android
WHAT: Удаление записи/веса/сохранённого блюда становится обратимым: сущность удаляется сразу и предлагается snackbar 'Отменить', возвращающий её; confirm-диалоги удаления заменяются этим механизмом.
LAYERS: presentation
CHANGED_HINT:
  - app/src/main/java/com/k/shavrin/diethelper/MainActivity.kt:32,37 — rememberSnackbarHostState() + snackbarHost-параметр в root Scaffold (G10, G18)
  - app/src/main/java/com/k/shavrin/diethelper/presentation/screen/today/TodayViewModel.kt:133 — deleteEntry: держать последнюю удалённую FoodEntry, метод undoDelete() re-insert через Add-UseCase; событие показа snackbar (G9; D1)
  - app/src/main/java/com/k/shavrin/diethelper/presentation/screen/weight/WeightViewModel.kt:69 — то же для WeightEntry (G9; D1)
  - app/src/main/java/com/k/shavrin/diethelper/presentation/screen/product/ProductViewModel.kt:103 — то же для SavedMeal (G9; D1)
  - app/src/main/java/com/k/shavrin/diethelper/presentation/screen/today/TodayScreen.kt:834, today/TodayDesignedContent.kt:868, weight/WeightScreen.kt:147, product/ProductSearchScreen.kt:240 — убрать confirm-AlertDialog, удаление сразу + snackbar-событие (G8; D1, H1)
  - app/src/test/java/com/k/shavrin/diethelper/presentation/screen/weight/WeightViewModelTest.kt:104 — добавить кейс undo: delete→undoDelete→сущность вернулась (G17)
TEST_TYPES: unit, compose-ui
CONSTRAINTS:
  - Depends-on ux-polish-01: MainActivity правит и 01 (installSplashScreen) — строго после
  - ⚠ Same-file: WeightScreen/TodayScreen трогают также 03/04/07 — строгая цепочка
  - re-insert через существующие Add/Upsert UseCase (D1) — НЕ менять DAO/схему; удалённая сущность живёт в памяти VM до истечения snackbar (O2 — длительность на усмотрение)
  - Строки snackbar/'Отменить' — из ресурсов (i18n прошёл, H4)
  - UX-контракт меняется: удаление теперь мгновенное+обратимое (не превентивное подтверждение) — отражено в acceptance (H1)
=== END SPEC ===

## Acceptance
```gherkin
Feature: Deletions are reversible via snackbar
  Covers epic ux-polish, SPEC 02.

  @ux-polish-02
  Scenario: Deleting an entry offers an undo
    Given a food entry is shown
    When the user deletes it
    Then the entry disappears and an undo snackbar is shown

  @ux-polish-02
  Scenario: Undo restores the deleted entry
    Given an entry was just deleted and the undo snackbar is visible
    When the user taps undo
    Then the entry is restored

  @ux-polish-02 @edge
  Scenario: Letting the snackbar expire keeps the deletion
    Given an entry was deleted and the undo snackbar is visible
    When the snackbar is dismissed without undo
    Then the entry remains deleted
```

## Gap / context
Удаление fire-and-forget через confirm-диалог (G8, G9), отката нет, SnackbarHost отсутствует (G10) — ошибочное удаление необратимо.

## Implementation links
- commit: (pending)
- files:  (pending)
