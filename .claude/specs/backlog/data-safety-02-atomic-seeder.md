# Атомарный DatabaseSeeder + первые тесты сидера
Epic: data-safety
Order: 02 of 08
Status: backlog
Depends-on: —
Date: 2026-07-06

## SPEC
=== SPEC ===
TASK: bugfix
PLATFORM: android
WHAT: Крэш посреди первого сида не должен оставлять базу в частично засеянном состоянии; повторный запуск досеивает с чистого листа. Появляются первые unit-тесты DatabaseSeeder.
LAYERS: data
CHANGED_HINT:
  - app/src/main/java/com/k/shavrin/diethelper/data/local/DatabaseSeeder.kt:14-18 — обернуть цикл вставок DEFAULT_PRODUCTS в db.withTransaction { ... }; goalsDataSource.setSeeded() вызывать ПОСЛЕ успешного коммита транзакции (G5, G6; locked decision D5)
  - DatabaseSeeder — добавить в конструктор DietHelperDatabase (для withTransaction); Hilt разрешит зависимость автоматически, DatabaseModule уже провайдит БД (G5, G33)
  - app/src/test/java/com/k/shavrin/diethelper/data/local/DatabaseSeederTest.kt — НОВЫЙ: Robolectric + in-memory Room по паттерну DAO-тестов (G12); кейсы: свежая БД сеется; повторный вызов не дублирует; сбой посреди сида не оставляет частичных продуктов (rollback)
TEST_TYPES: unit, dao
CONSTRAINTS:
  - withTransaction — корутинное расширение Room (room-ktx): проверить наличие артефакта в libs.versions.toml, при отсутствии добавить androidx.room:room-ktx той же версии 2.7.1 (assumption, hole H3)
  - Кросс-стор атомарность (флаг в DataStore, данные в Room) недостижима — принято D5: rollback транзакции делает повторный сид безопасным; НЕ переносить флаг в БД (это изменение схемы — вне скоупа)
  - В тесте сидера DataStore-флаг мокать нельзя (fakes-only) — использовать реальный PreferenceDataStoreFactory с временным файлом или тестовый двойник GoalsDataSource-уровня; выбор за тестером, зафиксировать комментарием
  - DAO-тесты обязаны использовать @Config(application = android.app.Application::class), иначе продовый сидер конфликтует с in-memory БД (G12)
=== END SPEC ===

## Acceptance
```gherkin
Feature: Atomic first-run seeding
  Covers epic data-safety, SPEC 02.

  @data-safety-02
  Scenario: First launch seeds the default product catalog
    Given the app starts on a fresh install
    When the initial seeding completes
    Then the product catalog contains the full default set
    And the seeded flag is recorded

  @data-safety-02 @error
  Scenario: A crash during seeding leaves no partial catalog
    Given the app starts on a fresh install
    When seeding is interrupted before completion
    Then the product catalog contains no partially seeded products
    And the seeded flag is not recorded

  @data-safety-02
  Scenario: Second launch does not duplicate products
    Given the app has already been seeded
    When the app starts again
    Then the product catalog size is unchanged
```

## Gap / context
Флаг isSeeded ставится только после всех вставок без транзакции (G5): крэш посреди сида = половина каталога + повторный сид поверх. Тестов сидера не существует вовсе (G13).

## Implementation links
- commit: (pending)
- files:  (pending)
