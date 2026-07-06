# Quality gates — coverage, CI hardening, telemetry — epic overview
Epic: quality-gates
Order: 00 of 05
Status: backlog
Depends-on: — (SPEC 01 ramp-порог удобнее ставить ПОСЛЕ эпика testability, когда покрытие вырастет; но эпик самодостаточен)
Date: 2026-07-06

## Goal
Превратить существующие, но негейтящие проверки в реальные гейты и заполнить пробелы CI/инфраструктуры (аудит 2026-07-06): JaCoCo без verification rule (покрытие ~27.7%), CI без wrapper-validation / release-smoke / dependabot, отсутствует androidTest, пустая телеметрия selfimprove. Вне скоупа: реальное доведение покрытия до 65% (это эпик testability), эмулятор-джоб, detekt-baseline.

## Locked decisions (из grill.md)
- D1: JacocoCoverageVerification с ramp от текущего — старт ≈ факту (замерить), гейт зелёный сразу, поднимать по мере testability; цель 65% в комментарии. [confirmed]
- D2: release-smoke = assembleRelease без подписи (R8/proguard-проверка; signingConfig не добавляем). [confirmed]
- D3: телеметрия — Stop-hook в .claude/settings.json зовёт record-run.sh + документированный контракт. [confirmed]
- D4: 6 пунктов аудита → 5 SPEC-ов; JaCoCo-задача (build.gradle.kts) отделена от CI-обвязки (ci.yml). [assumption]
- D5: androidTest — минимальный Hilt-smoke (запуск MainActivity, «не падает»); в CI НЕ гоняем (нет эмулятора), только компиляция. [assumption]

## SPECs (run via /mp --feature --next in Order)
| Order | File | Depends-on | Layers | Summary |
|---|---|---|---|---|
| 01 | `quality-gates-01-jacoco-verification-ramp.md` | — | build | JacocoCoverageVerification + ramp-порог (цель 65% в комментарии) |
| 02 | `quality-gates-02-ci-hardening.md` | 01 | ci | wrapper-validation + assembleRelease-smoke + coverage-шаг + retention |
| 03 | `quality-gates-03-dependabot.md` | — | ci | Dependabot для version catalog + github-actions |
| 04 | `quality-gates-04-androidtest-hilt-smoke.md` | 01 | build, test | androidTest-скаффолд: HiltTestRunner + 1 smoke-тест запуска |
| 05 | `quality-gates-05-selfimprove-telemetry-hook.md` | — | tooling | Stop-hook → record-run.sh; петля начинает писаться |

## Why this ordering
01 создаёт gradle-задачу verification — 02 её вызывает в CI (Depends-on) и 04 добавляет androidTest-зависимости в тот же build.gradle.kts (**same-file clash 01↔04 → 01 первым**). 02 — единственный правщик ci.yml. 03 и 05 независимы (новые файлы / другой конфиг). Каждый SPEC независимо шиппится.

## Key facts (verified — полный ledger: C:\Users\Admin\AppSpecs\quality-gates\pipeline\grounding.md)
- G1/G2: jacocoUnitTestReport есть (build.gradle.kts:133-164), verification rule НЕТ
- G3: ci.yml 3 джоба; нет wrapper-validation / assembleRelease / retention
- G4: gradle-wrapper 8.9, jar присутствует (wrapper-validation осмыслен)
- G5: release — minify+proguard, signingConfig НЕ задан → smoke без подписи
- G6/G7: jacoco-плагин не на root; single-module `:app`
- G8: libs.versions.toml [versions]/[libraries]/[plugins] — под dependabot
- G10: dependabot.yml / renovate.json отсутствуют
- G11: покрытие 27.7% историческое (STATE.md), цель 65%
- G12–G15: selfimprove/ = 6 файлов; record-run.sh не вшит; runs/ пуст; точка вшивания — конец runner-шага
- G16–G18: androidTest/ нет; testInstrumentationRunner дефолтный; @HiltAndroidApp:12, MainActivity @AndroidEntryPoint:24; AppNavHostTest — source-inspection, не реальный nav-тест
- G19: .gitignore:34 игнорит runs/*.jsonl (raw), lessons.md tracked

## Implementation links
- commit: (pending)
- files:  (pending)
