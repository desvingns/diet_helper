# selfimprove: Stop-hook вшивает телеметрию прогонов
Epic: quality-gates
Order: 05 of 05
Status: backlog
Depends-on: —
Date: 2026-07-06

## SPEC
=== SPEC ===
TASK: feature
PLATFORM: android
WHAT: Петля self-improvement начинает заполняться сама: по завершении сессии Stop-hook пишет один JSON-вердикт прогона в selfimprove/runs/ через record-run.sh.
LAYERS: tooling
CHANGED_HINT:
  - .claude/settings.json — добавить hooks.Stop: command, зовущий `bash selfimprove/record-run.sh --agent mp-orchestrator --verdict <pass|fail|partial> [--metric ...]`; вердикт из результата прогона (G13, G15; D3)
  - .claude/settings.json — команда использует абсолютный путь к репо/скрипту ИЛИ гарантирует CWD=корень репо, иначе runs/ не найдётся (H5)
  - selfimprove/README.md — уточнить раздел wire-in: зафиксировать, что вшивание сделано через Stop-hook, с примером и разъяснением ярлыка --agent (G15, O2)
TEST_TYPES: unit
CONSTRAINTS:
  - record-run.sh уже существует и не трогается (G13) — SPEC только вызывает его; runs/*.jsonl остаётся gitignored (G19)
  - Hook пишет ВСЕГДА один раз за Stop; не блокировать выход сессии на ошибке скрипта (best-effort, `|| true`)
  - Ярлык --agent и способ извлечения вердикта — на усмотрение реализации (O2); минимально приемлемо: фиксировать pass, метрику опустить
  - Это репо-локальная настройка процесса, НЕ код приложения — app/src не трогается; проверка = появление строки в runs/YYYY-MM.jsonl после прогона
=== END SPEC ===

## Acceptance
```gherkin
Feature: Self-improvement telemetry captures runs
  Covers epic quality-gates, SPEC 05.

  @quality-gates-05
  Scenario: A finished session records a run
    Given the telemetry Stop-hook is configured
    When a session finishes
    Then a JSON verdict line is appended to the current month's runs file

  @quality-gates-05 @edge
  Scenario: A telemetry failure does not block the session
    Given the record-run script fails for any reason
    When a session finishes
    Then the session still exits normally
```

## Gap / context
record-run.sh не вшит никуда (G14): runs/ пуст с 2026-05-29, lessons.md пуст — петля observe→reflect→propose не может стартовать без данных.

## Implementation links
- commit: (pending)
- files:  (pending)
