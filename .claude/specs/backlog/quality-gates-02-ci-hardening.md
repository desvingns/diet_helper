# CI hardening: wrapper-validation + release-smoke + coverage-gate + retention
Epic: quality-gates
Order: 02 of 05
Status: backlog
Depends-on: quality-gates-01
Date: 2026-07-06

## SPEC
=== SPEC ===
TASK: feature
PLATFORM: android
WHAT: CI-workflow: валидирует gradle-wrapper (supply-chain), гоняет release-сборку как smoke на R8/proguard, гейтит покрытие, задаёт срок хранения артефактов.
LAYERS: ci
CHANGED_HINT:
  - .github/workflows/ci.yml — добавить step gradle/wrapper-validation-action@v3 (или actions/wrapper-validation) первым в каждый джоб с checkout; wrapper.jar присутствует, версия 8.9 (G3, G4)
  - .github/workflows/ci.yml — в build-джоб добавить `./gradlew :app:assembleRelease` как smoke (R8+proguard, БЕЗ установки/подписи — signingConfig не нужен для сборки) (G5; D2, H3)
  - .github/workflows/ci.yml — в unit-tests-джоб после jacocoUnitTestReport добавить `./gradlew :app:jacocoCoverageVerification` (задача из SPEC 01) (G1; depends_on 01)
  - .github/workflows/ci.yml — на upload-artifact шагах проставить retention-days (напр. 14) (G3)
TEST_TYPES: unit
CONSTRAINTS:
  - Depends-on quality-gates-01: шаг coverage-verification зовёт задачу, которой до 01 не существует
  - assembleRelease может упасть на нехватке -keep правил — это и есть цель smoke; при падении добавить минимальные -keep в proguard-rules.pro и задокументировать (H3, assumption)
  - Единственный правщик ci.yml в эпике — конфликтов нет; НЕ добавлять эмулятор-джоб (androidTest в CI вне скоупа, D5)
  - existing actions уже на @v4 (checkout/setup-java/upload-artifact) — не даунгрейдить (G3)
=== END SPEC ===

## Acceptance
```gherkin
Feature: Hardened CI workflow
  Covers epic quality-gates, SPEC 02.

  @quality-gates-02
  Scenario: A tampered gradle wrapper is rejected
    Given a pull request modifies the gradle wrapper jar to an unknown checksum
    When CI runs
    Then the wrapper-validation step fails the workflow

  @quality-gates-02
  Scenario: Release build is smoke-tested
    Given a pull request compiles cleanly in debug
    When CI runs the release assembly
    Then R8/proguard processing completes without errors

  @quality-gates-02 @error
  Scenario: Coverage regression blocks the merge
    Given a pull request drops coverage below the threshold
    When CI runs the coverage verification step
    Then the workflow fails
```

## Gap / context
CI (G3) не валидирует wrapper (supply-chain-риск), не собирает release (R8-падения всплывут только на релизе), не гейтит покрытие, артефакты копятся без срока.

## Implementation links
- commit: (pending)
- files:  (pending)
