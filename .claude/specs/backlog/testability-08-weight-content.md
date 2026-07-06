# WeightContent: извлечение + тесты + базлайны
Epic: testability
Order: 08 of 08
Status: backlog
Depends-on: testability-03
Date: 2026-07-06

## SPEC
=== SPEC ===
TASK: refactor
PLATFORM: android
WHAT: Экран веса извлекается в WeightContent(state, input, колбэки) — список с дельтами, ввод веса и подтверждение удаления становятся тестируемыми; 2 базлайна.
LAYERS: presentation
CHANGED_HINT:
  - app/src/main/java/com/k/shavrin/diethelper/presentation/screen/weight/WeightScreen.kt:46-189 — WeightScreen(viewModel) остаётся обёрткой, собирает ДВА flow (state + input :50-51, G38); тело → internal WeightContent(state: WeightUiState, input: String, onInputChange, onSave, onDelete), внутри существующие приватные WeightList :88, WeightRow :118 (с AlertDialog :147), DeltaText :165 (G32, G33; паттерн G21)
  - app/src/test/java/com/k/shavrin/diethelper/presentation/screen/weight/WeightScreenContentTest.kt — НОВЫЙ по G34: список показывает записи и дельты; пустое состояние 'Записей нет' (якорь G39); onSave по кнопке; подтверждение удаления вызывает onDelete
  - app/src/test/java/com/k/shavrin/diethelper/presentation/screenshot/WeightScreenScreenshotTest.kt — НОВЫЙ по G36/G49: 2 базлайна (список с дельтами + пустое состояние), darkTheme=true (D4)
TEST_TYPES: compose-ui
CONSTRAINTS:
  - Depends-on testability-03: конструктор WeightViewModel стабилизирован Clock-рефактором — извлечение поверх него исключает повторное трогание тестов VM
  - График веса (Canvas) — извлечь внутрь WeightContent как есть; semantics для Canvas НЕ добавлять (эпик a11y)
  - Silent-поведение невалидного ввода (WeightViewModel:63) не менять — фикс в эпике ux-polish; тест пиннит текущий контракт отображения, не валидацию
  - Новые базлайны коммитятся; CI verify зелёный (G48)
=== END SPEC ===

## Acceptance
```gherkin
Feature: Weight content is testable in isolation
  Covers epic testability, SPEC 08.

  @testability-08
  Scenario: Weight entries are listed with deltas
    Given weight entries with computed deltas exist
    When the weight content renders
    Then each entry shows its weight and delta

  @testability-08 @empty
  Scenario: Empty weight history explains itself
    Given no weight entries exist
    When the weight content renders
    Then the no-entries message is shown

  @testability-08
  Scenario: Confirmed deletion removes the entry
    Given a weight entry is shown
    When the user confirms its deletion
    Then the deletion is requested for that entry
```

## Gap / context
WeightScreen — inline UI с двумя flow и диалогом удаления (G32): ни одно из этих поведений не покрыто Compose-тестом, скриншотов экрана нет.

## Implementation links
- commit: (pending)
- files:  (pending)
