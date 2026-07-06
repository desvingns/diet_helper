# Dependabot для version catalog + github-actions
Epic: quality-gates
Order: 03 of 05
Status: backlog
Depends-on: —
Date: 2026-07-06

## SPEC
=== SPEC ===
TASK: feature
PLATFORM: android
WHAT: Обновления зависимостей и GitHub Actions приходят автоматическими PR-ами вместо ручного отслеживания дрейфа версий.
LAYERS: ci
CHANGED_HINT:
  - .github/dependabot.yml — НОВЫЙ: version:2; ecosystem "gradle" (Dependabot читает gradle/libs.versions.toml, G8), schedule weekly, target-branch main; ecosystem "github-actions" для .github/workflows (G3); ecosystem "docker" не нужен (G10)
  - .github/dependabot.yml — сгруппировать minor/patch в один PR (groups) чтобы не плодить шум; major — отдельными (assumption: конфиг-решение по снижению шума, не факт о репо)
TEST_TYPES: unit
CONSTRAINTS:
  - Dependabot требует, чтобы репо был на GitHub с включёнными Dependabot alerts — это настройка репозитория (вне кода); в SPEC отметить как ручной шаг в чеклисте верификатора
  - НЕ трогать сами версии в libs.versions.toml — dependabot будет предлагать их PR-ами, ревью человеком (human-gate культура)
  - Файл валидируется GitHub-ом при пуше; локального гейта нет — проверка = появление первого dependabot-PR
=== END SPEC ===

## Acceptance
```gherkin
Feature: Automated dependency updates
  Covers epic quality-gates, SPEC 03.

  @quality-gates-03
  Scenario: Outdated gradle dependency raises a PR
    Given a library in the version catalog has a newer release
    When Dependabot runs on its weekly schedule
    Then a pull request updating that library is opened

  @quality-gates-03
  Scenario: Outdated GitHub Action raises a PR
    Given a workflow action has a newer version
    When Dependabot runs
    Then a pull request bumping that action is opened

  @quality-gates-03 @edge
  Scenario: No PR when everything is current
    Given all dependencies and actions are already on their latest versions
    When Dependabot runs
    Then no update pull request is opened
```

## Gap / context
Ни dependabot.yml, ни renovate.json нет (G10): обновления безопасности и версий отслеживаются вручную, дрейф накапливается.

## Implementation links
- commit: (pending)
- files:  (pending)
