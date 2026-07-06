# Fail-fast миграции Room + история схемы
Epic: data-safety
Order: 01 of 08
Status: backlog
Depends-on: —
Date: 2026-07-06

## SPEC
=== SPEC ===
TASK: refactor
PLATFORM: android
WHAT: Будущий бамп версии БД без миграции должен падать сборкой/крэшем (fail-fast), а не молча стирать данные пользователя; история схемы Room коммитится в репозиторий.
LAYERS: data, di, build
CHANGED_HINT:
  - app/src/main/java/com/k/shavrin/diethelper/data/local/DietHelperDatabase.kt:28 — exportSchema=false → true (G1)
  - app/build.gradle.kts — добавить ksp-аргумент room.schemaLocation → "$projectDir/schemas" (G10; синтаксис ksp { arg(...) } для Room 2.7.1 — (assumption), сверить с документацией Room при реализации)
  - .gitignore:26 — убрать строку `schemas/`, каталог становится версионируемым (G10)
  - app/src/main/java/com/k/shavrin/diethelper/di/DatabaseModule.kt:32 — удалить вызов fallbackToDestructiveMigration(); addMigrations(MIGRATION_1_2) остаётся (G3, G4)
  - schemas/com.k.shavrin.diethelper.data.local.DietHelperDatabase/2.json — сгенерировать (:app:kspDebugKotlin) и закоммитить (G1, G10)
TEST_TYPES: dao, unit
CONSTRAINTS:
  - Схему v1 ретроспективно сгенерировать нельзя (exportSchema был false с рождения) — история начинается с 2.json; зафиксировать это комментарием в DietHelperDatabase (locked decision D2, hole H1)
  - Полноценный MigrationTestHelper-тест требует room-testing + androidTest-скаффолда — он появится в эпике quality-gates; здесь достаточно зелёных существующих DAO-тестов (G12) и сборки
  - Все CI-гейты остаются зелёными (G36); detekt: без FIXME-комментариев (G37)
=== END SPEC ===

## Acceptance
```gherkin
Feature: Fail-fast database migrations
  Covers epic data-safety, SPEC 01.

  @data-safety-01
  Scenario: App upgrade with a registered migration preserves user data
    Given a device has the app with database version 1 and recorded food entries
    When the user updates to a build with database version 2
    Then all previously recorded entries are still present

  @data-safety-01 @error
  Scenario: Missing migration fails loudly instead of wiping data
    Given a future build bumps the database version without registering a migration
    When the app opens the database
    Then the app fails with a migration error
    And no user data is silently deleted

  @data-safety-01
  Scenario: Schema history is tracked
    Given the project is built after this change
    Then the repository contains the exported schema file for version 2
```

## Gap / context
`fallbackToDestructiveMigration()` (G3) при любом будущем бампе версии без миграции молча стирает дневник питания; exportSchema=false (G1) лишает проект истории схемы и базы для автогенерации миграций.

## Implementation links
- commit: (pending)
- files:  (pending)
