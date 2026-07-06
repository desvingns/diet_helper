# JaCoCo verification rule + ramp-порог
Epic: quality-gates
Order: 01 of 05
Status: backlog
Depends-on: —
Date: 2026-07-06

## SPEC
=== SPEC ===
TASK: feature
PLATFORM: android
WHAT: Покрытие юнит-тестами становится гейтом: новая задача JacocoCoverageVerification падает при просадке ниже порога; порог стартует у текущего факта и растёт по мере наполнения тестами.
LAYERS: build
CHANGED_HINT:
  - app/build.gradle.kts — рядом с jacocoUnitTestReport (:133-164) добавить tasks.register<JacocoCoverageVerification>("jacocoCoverageVerification"): те же classDirectories/sourceDirectories/executionData и тот же exclude-список, violationRules { rule { limit { minimum = <старт> } } } (G1, G2)
  - app/build.gradle.kts — старт-порог замерить: сначала `./gradlew :app:jacocoUnitTestReport`, взять фактический % и поставить чуть ниже (напр. 0.25), в комментарии зафиксировать цель 0.65 и ramp-политику (G11; D1, O1, H2)
  - app/build.gradle.kts — jacoco-плагин применяется в :app (на root его нет, G6): убедиться, что задача видит плагин; при необходимости `plugins { jacoco }` в app-модуле
TEST_TYPES: unit
CONSTRAINTS:
  - Порог — РЕАЛЬНО замеренный, НЕ доверять исторической цифре 27.7% (может отличаться после эпиков data-safety/testability); комментарий обязателен (D1)
  - exclude-список ОБЯЗАН совпадать с report-задачей (Hilt/Room генерация), иначе verification считает по мёртвому коду (G1)
  - CI-вызов задачи — в SPEC 02 (не дублировать шаг здесь); эта задача только регистрируется
  - detekt/FIXME запрещён (G9); задача должна работать на `./gradlew :app:jacocoCoverageVerification`
=== END SPEC ===

## Acceptance
```gherkin
Feature: Coverage is gated with a ramped threshold
  Covers epic quality-gates, SPEC 01.

  @quality-gates-01
  Scenario: Coverage above threshold passes verification
    Given the current unit-test coverage is at or above the configured threshold
    When the coverage verification task runs
    Then the build succeeds

  @quality-gates-01 @error
  Scenario: A coverage regression fails the build
    Given a change drops coverage below the configured threshold
    When the coverage verification task runs
    Then the build fails with a coverage violation
```

## Gap / context
jacocoUnitTestReport только репортит (G2): просадку покрытия ничто не ловит, CI зелёный при любом %.

## Implementation links
- commit: (pending)
- files:  (pending)
