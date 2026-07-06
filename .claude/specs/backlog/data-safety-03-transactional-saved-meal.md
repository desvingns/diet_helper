# Транзакционная замена сохранённого блюда
Epic: data-safety
Order: 03 of 08
Status: backlog
Depends-on: —
Date: 2026-07-06

## SPEC
=== SPEC ===
TASK: bugfix
PLATFORM: android
WHAT: Пересохранение блюда с тем же именем атомарно: сбой между удалением старого и вставкой нового не может потерять блюдо пользователя.
LAYERS: data
CHANGED_HINT:
  - app/src/main/java/com/k/shavrin/diethelper/data/local/dao/SavedMealDao.kt — новый @Transaction suspend fun replaceMeal(meal: SavedMealEntity, items: List<SavedMealItemEntity>), внутри существующие deleteByName → insertMeal → insertItems (G9)
  - app/src/main/java/com/k/shavrin/diethelper/data/repository/SavedMealRepositoryImpl.kt:23-34 — saveMeal() делегирует dao.replaceMeal(...) вместо трёх отдельных вызовов (G8)
  - app/src/test/java/com/k/shavrin/diethelper/data/repository/SavedMealRepositoryImplTest.kt — обновить внутренний FakeSavedMealDao: добавить replaceMeal с той же семантикой (G14)
  - app/src/test/java/com/k/shavrin/diethelper/data/local/dao/SavedMealDaoTest.kt — тест replaceMeal: замена по имени сохраняет ровно одно блюдо с новыми позициями; FK-каскад позиций работает (G2, G12)
TEST_TYPES: dao, unit
CONSTRAINTS:
  - @Transaction на suspend-методе DAO с default-телом — штатный Room-паттерн; интерфейсный DAO требует abstract class ИЛИ default-метод интерфейса (Room 2.7 поддерживает @Transaction на default-методах интерфейса) — выбрать при реализации, зафиксировать комментарием (assumption)
  - Сигнатуру saveMeal в domain-интерфейсе SavedMealRepository НЕ менять — изменение только в data-слое (архитектурная вербозность сохраняется)
  - CI-гейты зелёные (G36)
=== END SPEC ===

## Acceptance
```gherkin
Feature: Atomic saved-meal replacement
  Covers epic data-safety, SPEC 03.

  @data-safety-03
  Scenario: Re-saving a meal under the same name replaces it
    Given a saved meal "Завтрак" with 2 items exists
    When the user saves a meal named "Завтрак" with 3 items
    Then exactly one saved meal named "Завтрак" exists
    And it contains the 3 new items

  @data-safety-03 @error
  Scenario: A failure during replacement does not lose the old meal
    Given a saved meal "Завтрак" with 2 items exists
    When replacement fails after the delete step
    Then the saved meal "Завтрак" with its 2 original items is still available
```

## Gap / context
saveMeal = deleteByName → insertMeal → insertItems без транзакции (G8, G9): сбой после delete теряет блюдо навсегда.

## Implementation links
- commit: (pending)
- files:  (pending)
